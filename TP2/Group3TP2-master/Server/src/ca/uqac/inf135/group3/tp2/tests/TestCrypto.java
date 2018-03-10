package ca.uqac.inf135.group3.tp2.tests;

import ca.uqac.inf135.group3.tp2.model.Message;
import ca.uqac.inf135.group3.tp2.crypto.*;

import java.util.Arrays;

public class TestCrypto {
    private static RSAKeyRing sp_keyRing;
    private static RSAKeyRing ot_keyRing;

    private static void loadRSAKeyRings() {
        try {
            sp_keyRing = RSAKeyManager.loadKeyRingFromFile("sp");
            ot_keyRing = RSAKeyManager.loadKeyRingFromFile("ottawa");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testRSA() {
        try {
            String testClear = "Sentence to encrypt.";
            Message clearMessage = new Message(testClear.getBytes());

            //Create a message
            System.out.println("Clear message: " + clearMessage.toString());

            //Encrypt it
            RSAMessage rsaMessage = new RSAMessage(clearMessage, sp_keyRing.getLocalPublicKey());
            //Display it to make sure it's encrypted
            System.out.println("Encrypted: " + rsaMessage.toString());

            //Decrypt it using our keypair (private key, actually)
            Message decrypted = rsaMessage.getClearMessage(sp_keyRing.getPrivateKey());

            //Display decrypted message
            System.out.println("Decrypted: " + decrypted.toString());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSHA() {
        try {
            //Construct the message to hash;
            String clearText = "This it the clear text to HASH";
            Message clearMessage = new Message(clearText.getBytes());
            System.out.println("Clear message: " + clearMessage.toString());

            //Generate a salt
            SHASalt salt = new SHASalt();

            //Hash the message
            SHADigest digest1 = new SHADigest(clearMessage, salt);
            System.out.println("SHA digest: " + digest1.toString());

            //Sign the digest with our private key
            RSAMessage signedDigest = new RSAMessage(digest1, ot_keyRing.getPrivateKey());

            //We transfer on the other side
            // Context:
            // We already know "salt" from the RSA exchange
            // We received "clearMessage" and "signedDigest", we want to make sure clearMessage is authentic and unaltered

            //We generate the digest ourself
            SHADigest digest2 = new SHADigest(clearMessage, salt);

            //We decrypt the signed digest using the sender's public key
            Message unsignedDigest = signedDigest.getClearMessage(ot_keyRing.getLocalPublicKey());

            //We compare digest2 to unsignedDigest
            if (digest2.matchDigest(unsignedDigest)) {
                System.out.println("OK digests are equals");
            }
            else {
                System.err.println("Digests do not match");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void testAES() {
        try {
            //Generate random key
            AESKey aesKey = new AESKey();

            System.out.println("Key: " + aesKey.toString());

            //That's the message we want to send out
            String clearText = "This is the message we want to send out encrypted with AES256.";
            Message clearMessage = new Message(clearText.getBytes());
            System.out.println("Clear: " + clearMessage.toString());

            //Encrypt in AES using the generated key
            AESMessage aesMessage = new AESMessage(clearMessage, aesKey);
            System.out.println("Encrypted: " + aesMessage.toString());

            //Move to the other side of the communication

            //Context: We received aesKey as a byte[] from the RSA exchange
            //Recreate the aesKey and make sure its the same from the other side
            AESKey aesKey2 = new AESKey(aesKey.get(), 0);
            //First, make sure aesKey2 is valid
            if (Arrays.equals(aesKey.get(), aesKey2.get())) {
                System.out.println("Keys are identical");
            }
            else {
                System.err.println("Keys are different");
            }


            //Context: We just received aesMessage, we want to retrieve the original message

            //Decrypt aesMessage using askKey2
            Message decryptedMessage = aesMessage.getClearMessage(aesKey2);
            System.out.println("Decrypted: " + decryptedMessage.toString());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Message testSendMessage(String text) {
        System.out.println("\n\nSouth-Pole\n");
        try {
            //That's the only keyRing we know
            final RSAKeyRing keyRing = sp_keyRing;

            //That's the clear message we want to send securely to Ottawa
            final Message message = new Message(text.getBytes());
            System.out.println("Original message: " + text);

            //We generate an AES key and an SHA salt
            final AESKey aesKey = new AESKey();
            System.out.println(String.format("AES Key size: %d", aesKey.length()));
            final SHASalt salt = new SHASalt();
            System.out.println(String.format("SHA Salt size: %d", salt.length()));

            //We merge aesKey and SHA salt together
            final Message mergedAesKeySalt = new Message(aesKey, salt);
            System.out.println(String.format("Merged aesKey and Salt size: %d", mergedAesKeySalt.length()));

            //And encrypt it in RSA, using Remote's public key (so that only Remote's private key can decrypt it later)
            final RSAMessage rsaSecretKeys = new RSAMessage(mergedAesKeySalt, keyRing.getRemotePublicKey());
            System.out.println(String.format("RSA secret keys size: %d", rsaSecretKeys.length()));
            //We no longer need mergedAesKeySalt
            mergedAesKeySalt.release();

            //We could sent rsaSecretKeys.get() to Ottawa right away

            //We hash the message
            final SHADigest hash = new SHADigest(message, salt);
            System.out.println(String.format("SHA hash size: %d", hash.length()));
            //We sign it using our private key
            final RSAMessage signedHash = new RSAMessage(hash, keyRing.getPrivateKey());
            System.out.println(String.format("Signed hash size: %d", signedHash.length()));
            //We no longer need hash
            hash.release();

            //We merge signed ash and the message
            final Message mergedSignedHashMessage = new Message(signedHash, message);
            System.out.println(String.format("Merged signed Hash and Message size: %d", mergedSignedHashMessage.length()));
            //We no longer need signedHash and message
            signedHash.release();
            message.release();

            //We encrypt it in AES using the previously generated AES key
            final AESMessage aesMessage = new AESMessage(mergedSignedHashMessage, aesKey);
            System.out.println(String.format("AES message size: %d", aesMessage.length()));
            //We no longer need mergedSignedHashMessage and aesKey
            mergedSignedHashMessage.release();
            aesKey.release();

            //We could sent aesMessage.get() to Ottawa right away

            //For testing purpose, we simply merge rsaSecretKeys and aesMessage before returning it
            final Message fullMessageToSend = new Message(rsaSecretKeys, aesMessage);
            System.out.println(String.format("Full message size: %d", fullMessageToSend.length()));
            //We no longer need rsaSecretKeys and aesMessage
            rsaSecretKeys.release();
            aesMessage.release();

            return fullMessageToSend;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void testReceiveMessage(Message received) {
        System.out.println("\n\nOttawa\n");
        try {
            //That's the only keyRing we know
            final RSAKeyRing keyRing = ot_keyRing;

            System.out.println(String.format("Received message size: %d", received.length()));

            //The first 512 bytes (4096 bits) is the RSA message
            final RSAMessage rsaSecretKeys = new RSAMessage(received.get(), 0, 512);
            System.out.println(String.format("RSA secret keys size: %d", rsaSecretKeys.length()));
            //Decrypt it using our private key to retrieve merged aesKey and salt
            final Message mergedAesKeySalt = rsaSecretKeys.getClearMessage(keyRing.getPrivateKey());
            System.out.println(String.format("Merged aesKey and Salt size: %d", mergedAesKeySalt.length()));
            //We no longer need rsaSecretKeys
            rsaSecretKeys.release();

            //The first 32 bytes (256 bits) is the AES256 key
            final AESKey aesKey = new AESKey(mergedAesKeySalt.get(), 0, AESKey.KEY_BYTES);
            System.out.println(String.format("AES Key size: %d", aesKey.length()));
            //The rest is the SHA salt
            final SHASalt salt = new SHASalt(mergedAesKeySalt.get(), AESKey.KEY_BYTES, SHASalt.SALT_LENGTH);
            System.out.println(String.format("SHA Salt size: %d", salt.length()));
            //We no longer need mergedAesKeySalt
            mergedAesKeySalt.release();

            //The rest (after the first 512 bytes) is the AES message
            final AESMessage aesMessage = new AESMessage(received.get(), 512);
            System.out.println(String.format("AES message size: %d", aesMessage.length()));
            //We no longer need received
            received.release();

            //Decrypt it using the previously obtained aesKey to retrieve the signed hash and the message
            final Message mergedSignedHashMessage = aesMessage.getClearMessage(aesKey);
            System.out.println(String.format("Merged signed Hash and Message size: %d", mergedSignedHashMessage.length()));
            //We no longer need aesMessage and aesKey
            aesMessage.release();
            aesKey.release();

            //The first 512 bytes (4096 bits) is the signed hash
            final RSAMessage signedHash = new RSAMessage(mergedSignedHashMessage.get(), 0,512);
            System.out.println(String.format("Signed hash size: %d", signedHash.length()));
            //The rest (after the first 512 bytes) is the message itself
            final Message message = new Message(mergedSignedHashMessage.get(), 512);
            //We no longer need mergedSignedHashMessage
            mergedSignedHashMessage.release();

            System.out.println("Received message: " + message.toString());

            //Now validate if the message really comes from South Pole

            //We hash the message by ourself
            final SHADigest hash = new SHADigest(message, salt);
            System.out.println(String.format("SHA hash size: %d", hash.length()));

            //We decrypt the signed hash using Remote's public key
            final Message hash0 = signedHash.getClearMessage(keyRing.getRemotePublicKey());
            //We no longer need signedHash
            signedHash.release();

            //We finally compare both hashs to make sure they match
            System.out.println("Computed hash : " + hash.toString());
            System.out.println("Decrypted hash: " + hash0.toString());
            if (hash.matchDigest(hash0)) {
                System.out.println("Message really comes from South Pole.");
            }
            else {
                System.err.println("Error! Integrity and authenticity of the message could not be verified.");
            }

            //We no longer need hash and hash0
            hash.release();
            hash0.release();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        loadRSAKeyRings();
        testRSA();
        testSHA();
        testAES();
        final Message message = testSendMessage("This is my super top secret message!");
        testReceiveMessage(message);
    }
}
