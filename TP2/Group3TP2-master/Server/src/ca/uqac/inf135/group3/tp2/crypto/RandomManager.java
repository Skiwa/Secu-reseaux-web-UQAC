package ca.uqac.inf135.group3.tp2.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomManager {
    private static final String ALGORITHM = "SHA1PRNG";

    public static byte[] getBytes(int bytes) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM);

            byte[] salt = new byte[bytes];
            secureRandom.nextBytes(salt);
            return salt;
        }
        catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Internal error, '%s' algorithm should exist", ALGORITHM));
            return null;
        }
    }
}
