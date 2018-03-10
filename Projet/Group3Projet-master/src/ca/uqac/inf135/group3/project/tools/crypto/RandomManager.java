package ca.uqac.inf135.group3.project.tools.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomManager {
    private static final String RANDOM_ALGO = "SHA1PRNG";

    private final static SecureRandom random;
    static {
        SecureRandom tmpRandom;
        try {
            tmpRandom = SecureRandom.getInstance(RANDOM_ALGO);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Secure random algorithm '%s' could not be found.", RANDOM_ALGO));
            e.printStackTrace();
            tmpRandom = null;
        }
        random = tmpRandom;
    }

    public static byte[] getBytes(int byteCount) {
        if (random != null) {
            final byte[] nextBytes = new byte[byteCount];
            random.nextBytes(nextBytes);
            return nextBytes;
        }

        return null;
    }
}
