package ca.uqac.inf135.group3.tp2.crypto;

import ca.uqac.inf135.group3.tp2.model.Message;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHADigest extends Message {
    public static final String ALGORITHM = "SHA-512";
    public static final int HASH_SIZE = 512;
    public static final int HASH_BYTES = HASH_SIZE / 8;

    private static byte[] getSHABytes(Message messageToHash) {
        try {
            //Prepare SHA hasher
            MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM);

            //Digest message
            return messageDigest.digest(messageToHash.get());
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }

    public SHADigest(byte[] digest) {
        super(digest);
    }
    public SHADigest(byte[] digest, int offset, int length) {
        super(digest, offset, length);
    }
    public SHADigest(byte[] digest, int offset) {
        super(digest, offset);
    }
    public SHADigest(Message messageToHash, SHASalt salt) {
        //Merge salt and message, then get a hash of it all
        this(getSHABytes(new Message(salt, messageToHash)));
    }

    //Can be compared to another digest or any other message
    public boolean matchDigest(Message otherDigest) {
        byte[] thisBytes = this.get();
        byte[] thatBytes = otherDigest.get();

        //Digest must be good length
        if (thisBytes.length != HASH_BYTES || thatBytes.length != HASH_BYTES) {
            return false;
        }

        //Digest must be equals byte to byte
        int len = thisBytes.length;
        for (int i = 0; i < len; ++i) {
            if (thisBytes[i] != thatBytes[i]) {
                return false;
            }
        }

        //OK, they are equals
        return true;
    }
}
