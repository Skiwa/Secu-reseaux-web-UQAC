package ca.uqac.inf135.group3.tp2;

import ca.uqac.inf135.group3.tp2.crypto.AESKey;
import ca.uqac.inf135.group3.tp2.crypto.AESMessage;
import ca.uqac.inf135.group3.tp2.crypto.RSAKeyManager;
import ca.uqac.inf135.group3.tp2.crypto.RSAKeyRing;
import ca.uqac.inf135.group3.tp2.model.HostPort;
import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.encryptionrelay.ClearMessageListenerTask;
import ca.uqac.inf135.group3.tp2.task.decryptionrelay.EncryptedMessageListenerTask;
import ca.uqac.inf135.group3.tp2.task.multicastlistener.MulticastReceiverTask;

public class SecuRelay {
    private static final String DEFAULT_REMOTE_HOST = "rpiexplorer.io";
    private static final int DEFAULT_ENCRYPTED_PORT = 8080;
    private static final String DEFAULT_KEYRING_PREFIX = "sp";
    private static final String DEFAULT_MULTICAST_HOST = "239.255.1.1";
    private static final int DEFAULT_MULTICAST_PORT = 6666;

    private static boolean isJCEAvailable() {
        try {
            final String test_data = "test";

            //Simply try to crypt and decrypt a message using AES, if an exception is raised, JCE is not installed
            Message clear = new Message(test_data.getBytes());
            AESKey aesKey = new AESKey();
            AESMessage encrypted = new AESMessage(clear, aesKey);
            Message decrypted = encrypted.getClearMessage(aesKey);

            //Also make sure decrypted message is valid;
            return test_data.equals(decrypted.toString());
        }
        catch (Exception e) {
            return false;
        }
    }

    private static String getFullJavaVersion() {
        return System.getProperty("java.version");
    }

    private static int getJavaVersion() {
        String[] subVersions = getFullJavaVersion().split("[.]");
        if (subVersions.length >= 2) {
            try {
                return Integer.parseInt(subVersions[1], 10);
            }
            catch (Exception e) {
                //Let the default return do it's job
            }
        }
        return 0;
    }

    private static void showSyntax() {
        System.out.println(              "Syntax: java -cp out ca.uqac.inf135.group3.tp2.SecuRelay INTRA:PORT NET [LISTEN_PORT [KEYRING_PREFIX [REMOTE[:PORT] [MULTICAST:[PORT]]]]]");
        System.out.println(              " INTRA:PORT       : INTRA is the interface accessing the intranet");
        System.out.println(              "                    PORT is the port listening for clear messages");
        System.out.println(              " NET              : The interface accessing the internet");
        System.out.println(String.format(" LISTEN_PORT      : (Optional) Port number on which to listen for encrypted incoming connexions (Default: %d)", DEFAULT_ENCRYPTED_PORT));
        System.out.println(String.format(" KEYRING_PREFIX   : (Optional) file name (without '.keyring' extension) to load as RSA keyring. Use either sp or ottawa. (Default: %s)", DEFAULT_KEYRING_PREFIX));
        System.out.println(String.format(" REMOTE[:PORT]    : (Optional) REMOTE hostname where encrypted message should be sent (Default: %s)", DEFAULT_REMOTE_HOST));
        System.out.println(String.format("                    (Optional) PORT number where encrypted message should be sent (Default: %d)", DEFAULT_ENCRYPTED_PORT));
        System.out.println(String.format(" MULTICAST[:PORT] : (Optional) MULTICAST group address where decrypted message should be sent (Default: %s)", DEFAULT_MULTICAST_HOST));
        System.out.println(String.format("                    (Optional) PORT number for multicast group where decrypted message should be sent (Default: %d)", DEFAULT_MULTICAST_PORT));
    }

    public static void main(String[] args) {
        System.out.println(String.format("Current version detected: %s", getFullJavaVersion()));
        System.out.println();

        if (!isJCEAvailable()) {

            final int version = getJavaVersion();
            switch (version) {
                case 6:
                    System.err.println("Unlimited Strength Java Cryptography Extension for Java 1.6 (JCE6) is not installed.");
                    System.err.println("See: http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html");
                    break;
                case 7:
                    System.err.println("Unlimited Strength Java Cryptography Extension for Java 1.7 (JCE7) is not installed.");
                    System.err.println("See: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html");
                    break;
                case 8:
                    System.err.println("Unlimited Strength Java Cryptography Extension for Java 1.8 (JCE8) is not installed.");
                    System.err.println("See: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html");
                    break;
                case 9:
                    System.err.println("Your Java 1.9 installation doe's not support unlimited strength cryptography.");
                    System.err.println("Java 1.9 should support it out of the box. Please try using Java 1.8 with JCE8.");
                    System.err.println("See: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html");
                    break;
                default:
                    System.err.println("Your current Java version is not supported, minimum supported version is Java 1.6.");
                    break;
            }
            return;
        }

        final Parameters parameters;

        //Read parameters
        if (args.length >= 2) {
            //Read keyring prefix
            final String keyringPrefix;
            if (args.length >= 4) {
                //KEYRING_PREFIX is specified
                keyringPrefix = args[3];
            }
            else {
                keyringPrefix = DEFAULT_KEYRING_PREFIX;
            }

            //Load keyring
            final RSAKeyRing keyRing;
            try {
                keyRing = RSAKeyManager.loadKeyRingFromFile(keyringPrefix);
            }
            catch (Exception e) {
                System.err.println(String.format("Unable to load keyring file: %s", RSAKeyManager.getKeyRingFileName(keyringPrefix)));
                showSyntax();
                return;
            }

            //Read INTRA:PORT parameter for Clear
            final HostPort clearListen = HostPort.parse(args[0]);
            if (clearListen == null) {
                System.err.println("Invalid INTRA:PORT parameter");
                showSyntax();
                return;
            }

            final HostPort encryptedListen;
            //Note: We use NET parameter as host
            final String netHost = args[1];
            if (args.length >= 3) {
                //LISTEN_PORT is specified
                final String concatHostPort = String.format("%s:%s", netHost, args[2]);
                encryptedListen = HostPort.parse(concatHostPort);
                if (encryptedListen == null) {
                    System.err.println("Invalid NET or LISTEN_PORT parameter");
                    showSyntax();
                    return;
                }
            }
            else {
                encryptedListen = new HostPort(netHost, DEFAULT_ENCRYPTED_PORT);
            }

            final HostPort encryptedTarget;
            if (args.length >= 5) {
                //REMOTE[:PORT] is specified
                encryptedTarget = HostPort.parse(args[4], DEFAULT_ENCRYPTED_PORT);
                if (encryptedTarget == null) {
                    System.err.println("Invalid REMOTE[:PORT] parameter");
                    showSyntax();
                    return;
                }
            }
            else {
                encryptedTarget = new HostPort(DEFAULT_REMOTE_HOST, DEFAULT_ENCRYPTED_PORT);
            }

            final HostPort multicastTarget;
            if (args.length >= 6) {
                //MULTICAST[:PORT] is specified
                multicastTarget = HostPort.parse(args[5], DEFAULT_MULTICAST_PORT);
            }
            else {
                multicastTarget = new HostPort(DEFAULT_MULTICAST_HOST, DEFAULT_MULTICAST_PORT);
            }

            parameters = new Parameters(clearListen, encryptedTarget, encryptedListen, multicastTarget, keyRing);

            //Display parameters
            System.out.println();
            System.out.println(String.format("RSA keyring loaded from file: %s", RSAKeyManager.getKeyRingFileName(keyringPrefix)));
            System.out.println();
            System.out.println("Clear to encrypted parameters:");
            System.out.println(String.format("  Will listen for clear messages from intranet on: %s", parameters.getClearMsgListen()));
            System.out.println(String.format("  Will send out encrypted messages toward the internet to: %s", parameters.getEncryptedMsgTarget()));
            System.out.println(String.format("  Will send out encrypted messages using interface: %s", parameters.getEncryptedMsgListen().getHostname()));
            System.out.println();
            System.out.println("Encrypted to clear parameters:");
            System.out.println(String.format("  Will listen for encrypted messages from internet on port: %s", parameters.getEncryptedMsgListen()));
            System.out.println(String.format("  Will diffuse decrypted messages to multicast group: %s", parameters.getMulticastTarget()));
            System.out.println(String.format("  Will diffuse decrypted messages using interface: %s", parameters.getClearMsgListen().getHostname()));
            System.out.println();
        }
        else {
            System.err.println("Missing parameters.");
            showSyntax();
            return;
        }

        //Start persistent services
        //NOTE: Since they are persistent, do not use TaskPool pour them

        //Create the intranet server (clear messages)
        new Thread(new ClearMessageListenerTask(parameters)).start();

        //Create the Internet server (encrypted messages)
        new Thread(new EncryptedMessageListenerTask(parameters)).start();

        //Create a simple multicast receiver to show received messages
        new Thread(new MulticastReceiverTask(parameters)).start();
    }
}
