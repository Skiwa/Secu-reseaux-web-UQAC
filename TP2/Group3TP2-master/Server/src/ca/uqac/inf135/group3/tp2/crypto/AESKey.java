package ca.uqac.inf135.group3.tp2.crypto;

import ca.uqac.inf135.group3.tp2.model.Message;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKey extends Message {
    public static final String ALGORITHM = "AES";
    public static final int KEY_SIZE = 256; //bits
    public static final int KEY_BYTES = KEY_SIZE / 8; //bytes

    private final SecretKey key;

    private static byte[] generateKey() {
        return RandomManager.getBytes(KEY_BYTES);
    }

    private static SecretKey keyFromBytes(byte[] rawBytes) {
        return new SecretKeySpec(rawBytes, ALGORITHM);
    }

    //Construct from raw bytes
    public AESKey (byte[] rawBytes) {
        super(rawBytes);
        this.key = keyFromBytes(get());
    }
    public AESKey(byte[] rawBytes, int offset, int length) {
        super(rawBytes, offset, length);
        this.key = keyFromBytes(get());
    }
    public AESKey(byte[] rawBytes, int offset) {
        super(rawBytes, offset);
        this.key = keyFromBytes(get());
    }
    //Generate a random AES256 key
    public AESKey() {
        this(generateKey());
    }

    public SecretKey getKey() {
        return key;
    }

}
