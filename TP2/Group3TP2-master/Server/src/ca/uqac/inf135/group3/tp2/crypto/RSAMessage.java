package ca.uqac.inf135.group3.tp2.crypto;

import ca.uqac.inf135.group3.tp2.model.Message;

import javax.crypto.Cipher;
import java.security.*;

public class RSAMessage extends Message {
    public static final String ALGORITHM = "RSA";

    // Encrypt a clear message using the target's public key so only it's private key can decrypt it later
    private static byte[] getEncryptedBytes(Message clearMessage, Key key) throws GeneralSecurityException {
        //Prepare RSA cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        //Perform encryption
        return cipher.doFinal(clearMessage.get());
    }

    public RSAMessage(byte[] encryptedBytes) {
        super(encryptedBytes);
    }
    public RSAMessage(byte[] encryptedBytes, int offset, int length) {
        super(encryptedBytes, offset, length);
    }
    public RSAMessage(byte[] encryptedBytes, int offset) {
        super(encryptedBytes, offset);
    }
    public RSAMessage(Message clearMessage, Key key) throws GeneralSecurityException {
        this(getEncryptedBytes(clearMessage, key));
    }

    // Decrypt to clear Message using provided key
    public Message getClearMessage(Key key) throws GeneralSecurityException {
        //Prepare RSA cipher
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        //Perform decryption
        byte[] decryptedBytes = cipher.doFinal(get());
        return new Message(decryptedBytes);
    }


}
