package ca.uqac.inf135.group3.tp2.crypto;

import ca.uqac.inf135.group3.tp2.model.Message;

public class SHASalt extends Message {
    public static final int SALT_LENGTH = 64; //bytes

    private static byte[] generateSalt() {
        return RandomManager.getBytes(SALT_LENGTH);
    }

    //Construct from raw bytes
    public SHASalt (byte[] rawBytes) {
        super(rawBytes);
    }
    public SHASalt(byte[] rawBytes, int offset, int length) {
        super(rawBytes, offset, length);
    }
    public SHASalt(byte[] rawBytes, int offset) {
        super(rawBytes, offset);
    }
    //Generate a random AES256 key
    public SHASalt() {
        this(generateSalt());
    }

}
