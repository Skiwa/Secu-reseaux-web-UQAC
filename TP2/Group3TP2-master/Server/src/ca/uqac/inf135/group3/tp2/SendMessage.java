package ca.uqac.inf135.group3.tp2;

import java.net.Socket;

public class SendMessage {

    public static void main(String[] args) {
        if (args.length >= 3) {
            final int port;
            try {
                port = Integer.parseInt(args[1], 10);
            }
            catch (Exception e) {
                System.err.println("Invalid port number: " + args[1]);
                return;
            }

            final Socket sendSocket;
            try {
                sendSocket = new Socket(args[0], port);
            }
            catch (Exception e) {
                System.err.println(String.format("Unable to open send socket to %s:%d - Error: ", args[0], port, e.getMessage()));
                e.printStackTrace();
                return;
            }

            byte[] message = args[2].getBytes();

            try {
                sendSocket.getOutputStream().write(message);
            }
            catch (Exception e) {
                System.err.println(String.format("Error sending %d bytes message to %s:%d - Error: ", message.length, args[0], port, e.getMessage()));
                e.printStackTrace();
                return;
            }

            try {
                sendSocket.getOutputStream().flush();
            }
            catch (Exception e) {
                System.err.println("Error flushing message: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            try {
                sendSocket.close();
            }
            catch (Exception e) {
                System.err.println("Error closing socket: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            System.out.println("Message sent: " + args[2]);
        }
        else {
            System.err.println("Syntax: java -cp out ca.uqac.inf135.group3.tp2.SendMessage IP PORT \"MESSAGE\"");
        }

    }
}
