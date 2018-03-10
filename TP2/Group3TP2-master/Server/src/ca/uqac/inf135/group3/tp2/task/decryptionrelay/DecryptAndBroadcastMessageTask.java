package ca.uqac.inf135.group3.tp2.task.decryptionrelay;

import ca.uqac.inf135.group3.tp2.crypto.*;
import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.generic.MessageAcceptTask;

import java.io.IOException;
import java.net.*;

public class DecryptAndBroadcastMessageTask extends MessageAcceptTask {
    private static int MULTICAST_CHUNK_SIZE = 1400;

    public DecryptAndBroadcastMessageTask(Socket socket, Parameters parameters) {
        super(socket, parameters);
    }

    @Override
    protected String getMessageDescription() {
        return "remote encrypted message";
    }

    @Override
    protected void processMessage(Message rawMessage) {
        Message clearMessage = getClearAndValidatedMessage(rawMessage);

        if (clearMessage != null) {
            System.out.println(String.format("Successfully decrypted a %d bytes message, sending it to multicast group...", clearMessage.length()));
            sendMessageToMulticast(clearMessage);
            clearMessage.release();
        }
    }

    private Message getClearAndValidatedMessage(Message encryptedMessage) {
        final Parameters parameters = getParameters();

        try {   //Global exception catcher that will also release encryptedMessage
            //Validate the message length (should be more than the size of a two RSA blocs)
            final int encryptedLength = encryptedMessage.length();
            final int expectedEncryptedLength = 2 * RSAKeyManager.BLOC_SIZE;
            if (encryptedLength > expectedEncryptedLength) {
                //Extract the RSA message consisting of a single bloc (first 4096 bits = 512 bytes)
                final RSAMessage rsaMessage = new RSAMessage(encryptedMessage.get(), 0, RSAKeyManager.BLOC_SIZE);

                //Decrypt the RSA message using our private key (it was encrypted using our public key)
                final Message decryptedRSA;
                try {   //To release rsaMessage
                    decryptedRSA = rsaMessage.getClearMessage(parameters.getKeyRing().getPrivateKey());
                } catch (Exception e) {
                    throw new Exception(String.format("Error decrypting RSA message: %s", e.getMessage()), e);
                } finally {
                    //No matter what happened, we no longer need rsaMessage
                    rsaMessage.release();
                }

                final AESKey aesKey;
                final SHASalt salt;
                try {   //To release decryptedRSA
                    //Validate the length of the decrypted RSA message
                    final int decryptedRSAlenght = decryptedRSA.length();
                    final int expectedRSAlenght = AESKey.KEY_BYTES + SHASalt.SALT_LENGTH;
                    if (decryptedRSAlenght == expectedRSAlenght) {
                        //Extract the aesKey from the 32 first bytes (256 bits)
                        aesKey = new AESKey(decryptedRSA.get(), 0, AESKey.KEY_BYTES);
                        //Extract the SHA salt from the rest
                        salt = new SHASalt(decryptedRSA.get(), AESKey.KEY_BYTES);
                    } else {
                        throw new Exception(String.format("Decrypted RSA message (%d bytes) should be exactly %d bytes", decryptedRSAlenght, expectedRSAlenght));
                    }
                } finally {
                    //No matter what happened, we no longer need the decrypted RSA message
                    decryptedRSA.release();
                }

                try {   //To release salt
                    //Extract the AES message from the rest of the encryptedMessage
                    final AESMessage aesMessage = new AESMessage(encryptedMessage.get(), RSAKeyManager.BLOC_SIZE);

                    //Decrypt the aesMessage using the previously obtained rsaKey
                    final Message decryptedAES;
                    try {   //To release aesMessage and aesKey
                        decryptedAES = aesMessage.getClearMessage(aesKey);
                    } catch (Exception e) {
                        //If an exception was cough, we must release salt here cause we'll not need it
                        throw new Exception(String.format("Error decrypting AES message: %s", e.getMessage()), e);
                    } finally {
                        //No matter what happened, we no longer need neither aesMessage nor aesKey
                        aesMessage.release();
                        aesKey.release();
                    }

                    try {   //To release decryptedAES
                        //Validate decrypted AES message length (should be more than the size of a single RSA blocs)
                        final int decryptedAESLength = decryptedAES.length();
                        final int expectedDecryptedAESLength = RSAKeyManager.BLOC_SIZE;
                        if (decryptedAESLength > expectedDecryptedAESLength) {
                            //Extract the signed hash from the first RSA bloc
                            final RSAMessage signedHash = new RSAMessage(decryptedAES.get(), 0, RSAKeyManager.BLOC_SIZE);

                            //Try and decrypt it to retrieve the hash performed by the sender (decrypt it using remote's public key)
                            final Message flatHashByRemote;
                            try {   //To release signedHash
                                flatHashByRemote = signedHash.getClearMessage(parameters.getKeyRing().getRemotePublicKey());
                            } catch (Exception e) {
                                throw new Exception(String.format("An error occurred decrypting signed hash: %s", e.getMessage()), e);
                            } finally {
                                //No matter what happened, we no longer need signedHash
                                signedHash.release();
                            }

                            //Make it a SHADigest
                            final SHADigest hashByRemote = new SHADigest(flatHashByRemote.get());
                            flatHashByRemote.release();

                            //Extract the clear message from the decrypted AES message (right after the first RSA bloc)
                            final Message clearMessage = new Message(decryptedAES.get(), RSAKeyManager.BLOC_SIZE);

                            //Perform our own hash of that message
                            final SHADigest ownHash = new SHADigest(clearMessage, salt);
                            try {   //To release hashByRemote and ownHash
                                //Do hashes match?
                                if (ownHash.matchDigest(hashByRemote)) {
                                    //OK message is valid
                                    return clearMessage;
                                } else {
                                    throw new Exception("Local message hash mismatch remote's hash");
                                }
                            } finally {
                                hashByRemote.release();
                                ownHash.release();
                            }
                        } else {
                            throw new Exception(String.format("Decrypted AES message (%d bytes) is too short to be valid, should be more than %d bytes", decryptedAESLength, expectedDecryptedAESLength));
                        }
                    } finally {
                        decryptedAES.release();
                    }
                }
                finally {
                    salt.release();
                }
            }
            else {
                throw new Exception(String.format("Received message (%d bytes) is too short to be valid, should be more than %d bytes", encryptedLength, expectedEncryptedLength));
            }

        }
        catch (Exception e) {
            System.err.println(String.format("An error occurred decrypting an incoming message: %s", e.getMessage()));
        }
        finally {
            //We no longer need encryptedMessage
            encryptedMessage.release();
        }

        return null;
    }

    private void sendMessageToMulticast(Message clearMessage) {
        //Get some necessary parameters
        final Parameters parameters = getParameters();
        final String multicastGroupHost = parameters.getMulticastTarget().getHostname();
        final int multicastGroupPort = parameters.getMulticastTarget().getPort();
        final String localInterface = parameters.getClearMsgListen().getHostname();

        //Find an InetAddress from the multicast hostname
        final InetAddress groupIP;
        try {
            groupIP = InetAddress.getByName(multicastGroupHost);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve multicast group hostname '%s'", multicastGroupHost));
            return;
        }

        //Create an InetAddress for our "intra" interface
        final InetAddress intraInterface;
        try {
            intraInterface = InetAddress.getByName(localInterface);
        } catch (UnknownHostException e) {
            System.err.println(String.format("Unable to resolve local intranet-facing interface '%s'", localInterface));
            return;
        }

        //Create a multicast group socket
        final MulticastSocket multicastSocket;
        try {
            multicastSocket = new MulticastSocket();
            //Allow 15 hop (more than required)
            multicastSocket.setTimeToLive(15);
            //Use intranet-facing interface
            multicastSocket.setInterface(intraInterface);
        } catch (IOException e) {
            System.err.println("Error initializing multicast socket");
            return;
        }

        //Broadcast message on multicast socket
        try {
            final byte[] messageBytes = clearMessage.get();
            final byte[] bytesToSend = new byte[MULTICAST_CHUNK_SIZE];

            int sendOffset = 0;
            //As long as there's more data to send
            while (messageBytes.length - sendOffset > 0) {
                final int bytesCount;
                if (messageBytes.length - sendOffset > MULTICAST_CHUNK_SIZE) {
                    //Only get the first MULTICAST_CHUNK_SIZE bytes
                    bytesCount = MULTICAST_CHUNK_SIZE;
                }
                else {
                    //Take the rest of the message
                    bytesCount = messageBytes.length - sendOffset;
                }

                //Copy the selected part to the send array
                System.arraycopy(messageBytes, sendOffset, bytesToSend, 0, bytesCount);

                //Advance the offset
                sendOffset += bytesCount;

                //Create and send the packet
                final DatagramPacket packet = new DatagramPacket(bytesToSend, bytesCount, groupIP, multicastGroupPort);
                multicastSocket.send(packet);
            }
        }
        catch (Exception e) {
            System.err.println("Error broadcasting message to multicast socket: " + e.getMessage());
        }

    }
}
