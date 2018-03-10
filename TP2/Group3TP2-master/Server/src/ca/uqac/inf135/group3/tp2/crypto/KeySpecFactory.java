package ca.uqac.inf135.group3.tp2.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeySpecFactory {
    public static KeySpec getInstance(String format, byte[] encodedKey) throws InvalidAlgorithmParameterException {
        if ("PKCS#8".equals(format)) {
            return new PKCS8EncodedKeySpec(encodedKey);
        }
        else if ("X.509".equals(format)) {
            return new X509EncodedKeySpec(encodedKey);
        }

        throw new InvalidAlgorithmParameterException(String.format("'%s' is not a valid supported format.", format));
    }
}
