package ca.uqac.inf135.group3.project.model.entities.goasp;

import ca.uqac.inf135.group3.project.tools.crypto.HashMacManager;
import ca.uqac.inf135.group3.project.tools.crypto.RandomManager;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Base64;

@DatabaseTable(tableName = "user")
public class GoaspUser {
    private static final int SALT_LENGTH = 64; //64 bytes = 512 bits

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(unique = true, canBeNull = false)
    private String username;
    @DatabaseField(canBeNull = false)
    private String base64HashedPassword;
    @DatabaseField(canBeNull = false)
    private String base64Salt;
    @DatabaseField(unique = true, canBeNull = false)
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

    //ORM constructor
    public GoaspUser() {
    }
    //New user creation constructor
    public GoaspUser(String username, String clearPassword, String email) {
        this.id = 0;
        this.username = username;
        this.email = email;

        this.base64Salt = this.getRandomSalt();
        this.base64HashedPassword = this.getHashedPassword(clearPassword, this.base64Salt);
    }

    //GoaspUser API
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
        final byte[] saltBytes = RandomManager.getBytes(SALT_LENGTH);
        return getBase64(saltBytes);
    }

    private String getHashedPassword(String password, String base64Salt) {
        //Compute hash of the password
        //Use salt as secret key
        byte[] hash = HashMacManager.getHashMac(password.getBytes(), decodeBase64(base64Salt));

        //Return bas64 encoded hash
        return getBase64(hash);
    }

    //Default getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBase64HashedPassword() {
        return base64HashedPassword;
    }

    public void setBase64HashedPassword(String base64HashedPassword) {
        this.base64HashedPassword = base64HashedPassword;
    }

    public String getBase64Salt() {
        return base64Salt;
    }

    public void setBase64Salt(String base64Salt) {
        this.base64Salt = base64Salt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
