package ca.uqac.inf135.group3.tp2.crypto;

import ca.uqac.inf135.group3.tp2.model.Message;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

public class AESMessage extends Message {
    public static final String ALGORITHM = "AES";
    public static final String MODE = "CTR";
    public static final String PADDING = "NoPadding"; //"PKCS5Padding";
    public static final int BLOCK_SIZE = 16; //bytes

    private static String getFullAlgorithm() {
        return String.format("%s/%s/%s", ALGORITHM, MODE, PADDING);
    }

    private static byte[] getEncryptedBytes(Message clearMessage, AESKey key) throws GeneralSecurityException {
        //Prepare cipher (NOTE: since we are using OFB, we don't need an IV)
        Cipher cipher = Cipher.getInstance(getFullAlgorithm());
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[BLOCK_SIZE]);
        cipher.init(Cipher.ENCRYPT_MODE, key.getKey(), ivSpec);

        return cipher.doFinal(clearMessage.get());
    }

    public AESMessage(byte[] encryptedBytes) {
        super(encryptedBytes);
    }
    public AESMessage(byte[] encryptedBytes, int offset, int length) {
        super(encryptedBytes, offset, length);
    }
    public AESMessage(byte[] encryptedBytes, int offset) {
        super(encryptedBytes, offset);
    }
    public AESMessage(Message clearMessage, AESKey key) throws GeneralSecurityException {
        super(getEncryptedBytes(clearMessage, key));
    }

    public Message getClearMessage(AESKey key) throws GeneralSecurityException {
        //Prepare cipher (NOTE: since we are using OFB, we don't need an IV)
        Cipher cipher = Cipher.getInstance(getFullAlgorithm());
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[BLOCK_SIZE]);
        cipher.init(Cipher.DECRYPT_MODE, key.getKey(), ivSpec);

        return new Message(cipher.doFinal(get()));
    }
}
