package ca.uqac.inf135.group3.tp2.task.encryptionrelay;

import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.model.Parameters;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class SendEncryptedToRemoteTask implements Runnable {
    private enum AwaitingAction {
        None,
        Message,
        Close,
    }

    //NOTE: We use the message queue as a lock for synchronization
    private final Queue<Message> messageQueue = new LinkedList<>();
    private boolean readyToClose = false;
    private int totalSentBytes = 0;

    private final Parameters parameters;

    private Socket remoteSocket;

    public SendEncryptedToRemoteTask(Parameters parameters){
        this.parameters = parameters;
    }

    private boolean openRemoteSocket() {
        final String remoteHost = parameters.getEncryptedMsgTarget().getHostname();
        final int remotePort = parameters.getEncryptedMsgTarget().getPort();
        final String netInterface = parameters.getEncryptedMsgListen().getHostname();

        final InetAddress inetNetInterface;
        try {
            inetNetInterface = InetAddress.getByName(netInterface);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve Internet interface '%s'. Error: %s", netInterface, e.getMessage()));
            return false;
        }

        remoteSocket = new Socket();
        try {
            remoteSocket.bind(new InetSocketAddress(inetNetInterface, 0));
        } catch (IOException e) {
            System.err.println(String.format("Unable to bind outgoing socket to Internet interface '%s'. Error: %s", netInterface, e.getMessage()));
            remoteSocket = null;
            return false;
        }

        final SocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);

        try {
            remoteSocket.connect(socketAddress);
        } catch (IOException e) {
            System.err.println(String.format("Error connecting outgoing socket to remote host '%s' on port %d. Error: %s", remoteHost, remotePort, e.getMessage()));
            remoteSocket = null;
            return false;
        }

        return true;
    }

    private AwaitingAction getNextAwaitingAction() {
        synchronized (messageQueue) {
            if (!messageQueue.isEmpty()) {
                return AwaitingAction.Message;
            }
            if (readyToClose) {
                return AwaitingAction.Close;
            }
        }
        return AwaitingAction.None;
    }

    public boolean sendMessage(Message message) {
        if (remoteSocket != null) {
            //Retrieve message bytes
            final byte[] bytes = message.get();

            try {
                //Send message bytes
                OutputStream outputStream = remoteSocket.getOutputStream();
                outputStream.write(bytes);
                outputStream.flush();

                //Count sent bytes
                totalSentBytes += bytes.length;
            } catch (Exception e) {
                System.err.println(String.format("Error sending encrypted message (%d bytes) to remote host. Error: %s", bytes.length, e.getMessage()));
                return false;
            }

            //Success
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void run() {
        if (openRemoteSocket()) {

            //Continuously check if there's an action to perform
            boolean closeConnection = false;
            while (!closeConnection) {
                //Check for an action
                AwaitingAction nextAction = getNextAwaitingAction();

                //And perform it
                switch (nextAction) {
                    case Message:
                        final Message message;
                        synchronized (messageQueue) {
                            message = messageQueue.poll();
                        }
                        //Not supposed to happen, getNextAwaitingAction just made sure there was a message awaiting
                        if (message != null) {
                            if (!sendMessage(message)) {
                                //Error sending message, close connection
                                closeConnection = true;
                            }
                        }
                        break;
                    case Close:
                        closeConnection = true;
                        break;
                }
            }

            final String remoteHost = parameters.getEncryptedMsgTarget().getHostname();
            final int remotePort = parameters.getEncryptedMsgTarget().getPort();
            System.out.println(String.format("Sent %d encrypted bytes to remote host %s:%d.", totalSentBytes, remoteHost, remotePort));

            try {
                if (remoteSocket != null) {
                    remoteSocket.close();
                }
            } catch (IOException e) {
                //We don't care
            }
        }
    }

    public void enqueueMessage(Message message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
        }
    }

    public void enqueueClose() {
        synchronized (messageQueue) {
            readyToClose = true;
        }
    }
}
