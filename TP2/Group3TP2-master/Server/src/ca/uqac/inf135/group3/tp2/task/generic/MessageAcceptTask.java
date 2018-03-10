package ca.uqac.inf135.group3.tp2.task.generic;

import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.TaskPool;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public abstract class MessageAcceptTask implements Runnable {
    private static final int READ_BUFFER_SIZE = 4096;

    private final Socket socket;
    private final InputStream inputStream;
    private final Parameters parameters;

    private final byte[] readBuffer = new byte[READ_BUFFER_SIZE];

    protected MessageAcceptTask(Socket socket, Parameters parameters) {
        InputStream inputStream;

        this.socket = socket;
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            inputStream = null;
            System.err.println(String.format("Error opening input stream receiving an incoming %s", getMessageDescription()));
        }
        this.inputStream = inputStream;
        this.parameters = parameters;
    }

    private int readToBuffer() {
        if (inputStream != null) {
            final int readLen;
            try {
                readLen = inputStream.read(readBuffer, 0, READ_BUFFER_SIZE);
            } catch (IOException e) {
                System.err.println(String.format("Error opening input stream receiving an incoming %s", getMessageDescription()));
                return -1;
            }
            return readLen;
        }
        return -1;
    }

    @Override
    public void run() {
        //Reads incoming intranet transmissions

        byte[] receivedBytes = new byte[0];

        //Repeatedly read chunks of data
        int byteCount;
        while((byteCount = readToBuffer()) >= 0) {
            if (byteCount > 0) { //We don't want to allocate/copy if we don't have to
                //Allocate a new buffer large enough to hold the already received bytes AND the new ones
                byte[] tempBuffer = new byte[receivedBytes.length + byteCount];

                //Copy in tempBuffer what we already had
                System.arraycopy(receivedBytes, 0, tempBuffer, 0, receivedBytes.length);
                //Append what we just received
                System.arraycopy(readBuffer, 0, tempBuffer, receivedBytes.length, byteCount);  // copy current lot

                //Replace the receivedBytes by the tempBuffer
                receivedBytes = tempBuffer;
            }
        }

        //Whether we received an error or not, we close the socket connexion
        try {
            socket.close();
        }
        catch (Exception e) {/*We don't care if the socket close failed*/ }

        //OK we have the full buffer
        System.out.println(String.format("Received an incoming %s: %d bytes", getMessageDescription(), receivedBytes.length));

        //Process received message
        processMessage(new Message(receivedBytes));
    }

    protected Parameters getParameters() {
        return parameters;
    }

    protected abstract String getMessageDescription();

    protected abstract void processMessage(Message rawMessage);
}
