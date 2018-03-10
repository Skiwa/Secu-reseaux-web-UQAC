package ca.uqac.inf135.group3.tp2.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Message {

    private byte[] content;

    public Message(byte[] message, int offset, int length) {
        if (length > 0) {
            this.content = new byte[length];
            System.arraycopy(message, offset, this.content, 0, length);
        }
        else {
            this.content = new byte[0];
        }
    }
    public Message(byte[] message, int offset) {
        this(message, offset, message.length - offset);
    }
    public Message(byte[] message) {
        this(message, 0);
    }

    //Merge messages constructor
    public Message(Message... messages) {
        //Compute total message length
        int totalLength = 0;
        for (Message message : messages) {
            totalLength += message.get().length;
        }

        //Allocate necessary space
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(totalLength);

        //Copy messages to outputStream
        for (Message message : messages) {
            try {
                outputStream.write(message.get());
            } catch (IOException e) {
                System.err.println("Internal error, outputStream should be big enough.");
                e.printStackTrace();
                break;
            }
        }

        this.content = outputStream.toByteArray();
    }

    public byte[] get() {
        return content != null ? content : new byte[0];
    }

    public int length() {
        return content != null ? content.length : 0;
    }

    //Call whenever we no longer need the message to clear out message content and make storage available for GC
    public void release() {
        if (content != null) {
            for (int i = 0; i < content.length; ++i) {
                content[i] = 0;
            }
            content = null;
        }
    }

    @Override
    public String toString() {
        return content != null ? new String(content) : "";
    }
}
