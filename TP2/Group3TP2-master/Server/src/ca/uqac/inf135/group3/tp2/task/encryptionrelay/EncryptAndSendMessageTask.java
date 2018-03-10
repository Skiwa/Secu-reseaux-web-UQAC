package ca.uqac.inf135.group3.tp2.task.encryptionrelay;

import ca.uqac.inf135.group3.tp2.crypto.*;
import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.model.Parameters;
import ca.uqac.inf135.group3.tp2.task.TaskPool;
import ca.uqac.inf135.group3.tp2.task.generic.MessageAcceptTask;

import java.net.Socket;
import java.security.GeneralSecurityException;

public class EncryptAndSendMessageTask extends MessageAcceptTask {


    public EncryptAndSendMessageTask(Socket socket, Parameters parameters) {
        super(socket, parameters);
    }

    @Override
    protected String getMessageDescription() {
        return "local clear message";
    }

    @Override
    protected void processMessage(Message rawMessage) {
        final Parameters parameters = getParameters();
        final RSAKeyRing keyRing = parameters.getKeyRing();

        //Enqueue a send encrypted task right away, while we'll be busy encrypting message, the sender task will establish the connection
        final SendEncryptedToRemoteTask senderTask = new SendEncryptedToRemoteTask(parameters);
        TaskPool.submitTask(senderTask);

        try {
            // -- Preparing the AES key and the salt SHA -- //

            //We generate an AES key and an SHA salt
            final AESKey aesKey = new AESKey();
            final SHASalt salt = new SHASalt();

            //We merge aesKey and SHA salt together
            final Message mergedAesKeySalt = new Message(aesKey, salt);
            //And encrypt it in RSA, using remote's public key (so that only remote's private key can decrypt it later)
            final RSAMessage rsaSecretKeys;
            try {
                rsaSecretKeys = new RSAMessage(mergedAesKeySalt, keyRing.getRemotePublicKey());
            } catch (GeneralSecurityException e) {
                System.err.println(String.format("En error occurred while encrypting an RSA message with remote's public key: %s", e.getMessage()));
                return;
            }
            finally {
                //We no longer need mergedAesKeySalt
                mergedAesKeySalt.release();
            }

            //rsaSecretKeys (aesKey+salt) is ready to be sent, enqueue it
            senderTask.enqueueMessage(rsaSecretKeys);

            // -- Preparing the hash and the message-- //

            //We hash the message
            final SHADigest hash = new SHADigest(rawMessage, salt);
            //We sign it using our private key
            final RSAMessage signedHash;
            try {
                signedHash = new RSAMessage(hash, keyRing.getPrivateKey());
            } catch (GeneralSecurityException e) {
                System.err.println(String.format("En error occurred while signing the message hash using our private key: %s", e.getMessage()));
                return;
            }
            finally {
                //We no longer need hash and salt
                hash.release();
                salt.release();
            }

            //We merge signed ash and the message
            final Message mergedSignedHashMessage = new Message(signedHash, rawMessage);
            //We no longer need signedHash
            signedHash.release();

            //We encrypt it in AES using the previously generated AES key
            final AESMessage aesMessage;
            try {
                aesMessage = new AESMessage(mergedSignedHashMessage, aesKey);
            } catch (GeneralSecurityException e) {
                System.err.println(String.format("En error occurred while AES encrypting the signed hash and the message: %s", e.getMessage()));
                return;
            }
            finally {
                //We no longer need mergedSignedHashMessage and aesKey
                mergedSignedHashMessage.release();
                aesKey.release();
            }

            //aesMessage (AES encrypted signed hash + message) is ready to be sent, enqueue it
            senderTask.enqueueMessage(aesMessage);
        }
        finally {
            //We no longer need rawMessage
            rawMessage.release();

            //Sender task can terminate
            senderTask.enqueueClose();
        }

    }
}
