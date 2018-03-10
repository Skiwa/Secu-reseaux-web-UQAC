package ca.uqac.inf135.group3.tp2.task.generic;

import ca.uqac.inf135.group3.tp2.model.HostPort;
import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.TaskPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class MessageListenerTask implements Runnable {
    //Allow a queue of 10 connection requests
    private static final int SERVER_SOCKET_BACKLOG = 10;

    private final Parameters parameters;
    private final HostPort listenHostPort;

    public MessageListenerTask(Parameters parameters, HostPort listenHostPort) {
        this.parameters = parameters;
        this.listenHostPort = listenHostPort;
    }

    @Override
    public void run() {
        final String listenHost = listenHostPort.getHostname();
        final int listenPort = listenHostPort.getPort();

        //Resolve listening host name
        final InetAddress listenAddress;
        try {
            listenAddress = InetAddress.getByName(listenHost);
        }
        catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve server host '%s'", listenHost));
            e.printStackTrace();
            return;
        }

        //Open server socket port
        final ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(listenPort, SERVER_SOCKET_BACKLOG, listenAddress);
        }
        catch (IOException e) {
            System.err.println(String.format("Unable to open local server socket on interface '%s' and port %d.", listenHost, listenPort));
            e.printStackTrace();
            return;
        }
        System.out.println(String.format("Listening to port %d on interface '%s' for %s.", listenPort, listenHost, getDescription()));

        //Continuously accept connexions
        for(;;) {
            final Socket acceptSocket;
            try {
                acceptSocket = serverSocket.accept();
            }
            catch (IOException e) {
                //OK, we were unable to accept that connexion, let's try again
                continue;
            }

            //Create a task to accept the clear message and submit it
            Runnable task = getTask(acceptSocket);
            TaskPool.submitTask(task);

        }
    }

    protected Parameters getParameters() {
        return parameters;
    }

    protected abstract Runnable getTask(Socket acceptSocket);

    protected abstract String getDescription();
}
