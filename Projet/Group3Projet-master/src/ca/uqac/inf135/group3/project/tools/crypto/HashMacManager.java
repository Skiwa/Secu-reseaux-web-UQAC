package ca.uqac.inf135.group3.project.tools.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HashMacManager {
    private static final String MAC_ALGO = "HmacSHA256";

    private static final Mac macAlgo;
    static {
        Mac tmpAlgo;
        try {
            //Initialize HASH_MAC algo
            tmpAlgo = Mac.getInstance(MAC_ALGO);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Mac algorithm '%s' could not be found.", MAC_ALGO));
            e.printStackTrace();
            tmpAlgo = null;
        }
        macAlgo = tmpAlgo;
    }

    public static byte[] getHashMac(byte[] content, byte[] secret) {
        if (macAlgo != null) {
            try {
                //Initialize secret key
                SecretKeySpec secret_key = new SecretKeySpec(secret, MAC_ALGO);
                macAlgo.init(secret_key);

                //Compute hash of the password
                return macAlgo.doFinal(content);
            } catch (InvalidKeyException e) {
                System.err.println("Unable to create SecretKeySpec");
                e.printStackTrace();
            }
        }
        return null;
    }

}
