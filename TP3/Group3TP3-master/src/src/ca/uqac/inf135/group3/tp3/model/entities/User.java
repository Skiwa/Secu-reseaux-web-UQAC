package ca.uqac.inf135.group3.tp3.model.entities;

import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class User {
    private static final String RANDOM_ALGO = "SHA1PRNG";
    private static final int SALT_LENGTH = 64; //64 bytes = 512 bits
    private static final String MAC_ALGO = "HmacSHA256";

    private int id;
    private String username;
    private String base64HashedPassword;
    private String base64Salt;
    private String email;

    private static String getBase64(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
    private static byte[] decodeBase64(String b64) {
        try {
            return Base64.getUrlDecoder().decode(b64);
        }
        catch (Exception e) {
            return null;
        }
    }

    //New user creation constructor
    public User(String username, String clearPassword, String email) {
        this.id = 0;
        this.username = username;
        this.email = email;

        this.base64Salt = this.getRandomSalt();
        this.base64HashedPassword = this.getHashedPassword(clearPassword, this.base64Salt);
    }

    //Database data constructor (fields as stored)
    public User(int id, String username, String base64HashedPassword, String base64Salt, String email) {
        this.id = id;
        this.username = username;
        this.base64HashedPassword = base64HashedPassword;
        this.base64Salt = base64Salt;
        this.email = email;
    }

    //User API
    public boolean isValidPassword(String password) {
        final String rehashedPassword = this.getHashedPassword(password, this.base64Salt);

        return rehashedPassword != null && rehashedPassword.equals(base64HashedPassword);
    }
    public byte[] getSalt() {
        return decodeBase64(this.base64Salt);
    }
    public JSONObject getJSON() {
        return new JSONObject()
                .add("id", getId())
                .add("username", getUsername())
                .add("email", getEmail());
    }

    //Construction getters
    private String getRandomSalt() {
        final SecureRandom random;
        try {
            random = SecureRandom.getInstance(RANDOM_ALGO);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(String.format("Secure random algorithm '%s' could not be found.", RANDOM_ALGO));
            e.printStackTrace();
            return "";
        }

        final byte[] saltBytes = new byte[SALT_LENGTH];
        random.nextBytes(saltBytes);
        return getBase64(saltBytes);
    }

    private String getHashedPassword(String password, String base64Salt) {
        //Decode salt
        try {
            byte[] salt = decodeBase64(base64Salt);
            if (salt == null) {
                throw new Exception("Salt is null");
            }

            //Initialize HASH_MAC algo and key
            final Mac macAlgo = Mac.getInstance(MAC_ALGO);

            //Use salt as secret key
            SecretKeySpec secret_key = new SecretKeySpec(salt, MAC_ALGO);
            macAlgo.init(secret_key);

            //Compute hash of the password
            byte[] hash = macAlgo.doFinal(password.getBytes());

            //Return bas64 encoded hash
            return getBase64(hash);
        }
        catch (Exception e) {
            System.err.println(String.format("%s.getHashedPassword failed.", getClass().getName()));
            e.printStackTrace();
            return "";
        }
    }

    //Default getters
    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getBase64HashedPassword() {
        return base64HashedPassword;
    }
    public String getBase64Salt() {
        return base64Salt;
    }
    public String getEmail() {
        return email;
    }

    //Default setter
    public void setId(int id) {
        this.id = id;
    }
}
