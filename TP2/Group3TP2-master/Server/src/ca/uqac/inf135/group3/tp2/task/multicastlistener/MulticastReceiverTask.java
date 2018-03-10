package ca.uqac.inf135.group3.tp2.task.multicastlistener;

import ca.uqac.inf135.group3.tp2.model.Parameters;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class MulticastReceiverTask implements Runnable {
    private Parameters parameters;

    public MulticastReceiverTask(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void run() {
        //Get some necessary parameters
        final String multicastGroupHost = parameters.getMulticastTarget().getHostname();
        final int multicastGroupPort = parameters.getMulticastTarget().getPort();
        final String localInterface = parameters.getClearMsgListen().getHostname();

        //Find an InetAddress from the multicast hostname
        final InetAddress groupIP;
        try {
            groupIP = InetAddress.getByName(multicastGroupHost);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve multicast group hostname '%s'", multicastGroupHost));
            e.printStackTrace();
            return;
        }

        //Create an InetAddress for our "intra" interface
        final InetAddress intraInterface;
        try {
            intraInterface = InetAddress.getByName(localInterface);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve local intranet-facing interface '%s'", localInterface));
            e.printStackTrace();
            return;
        }

        //Create a multicast group socket
        final MulticastSocket multicastSocket;
        try {
            multicastSocket = new MulticastSocket(multicastGroupPort);
            //Use intranet-facing interface
            multicastSocket.setInterface(intraInterface);
            //Join group
            multicastSocket.joinGroup(groupIP);
        } catch (IOException e) {
            System.err.println("Error initializing listener multicast socket");
            e.printStackTrace();
            return;
        }

        System.out.println(String.format("Waiting for diffusion to multicast group %s:%d on interface '%s'", multicastGroupHost, multicastGroupPort, localInterface));

        byte[] receiveBuffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        //Continuously receive messages
        for(;;) {
            try {
                multicastSocket.receive(packet);
                final int len = packet.getLength();

                if (len > 0) {
                    System.out.println(String.format("Message received on multicast group: %s", new String(receiveBuffer, 0, len)));
                }
                else {
                    System.err.println("Received an empty or invalid multicast message");
                }
            }
            catch(Exception e) {
                System.err.println(String.format("An error occurred while receiving multicast message: %s", e.getMessage()));
            }
        }

    }
}
