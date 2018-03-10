package ca.uqac.inf135.group3.project.model.entities.goasp;

import ca.uqac.inf135.group3.project.tools.crypto.RandomManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Base64;

@DatabaseTable(tableName = "session")
public class GoaspSession {
    private static final int RANDOM_SESSION_ID_LENGTH = 64 / 4 * 3;    //We want a 64 bytes long base64 string by the end
    private static final int RANDOM_SECURITY_TOKEN_LENGTH = 64 / 4 * 3;    //We want a 64 bytes long base64 string by the end

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(foreign = true)
    private GoaspUser user;

    @DatabaseField()
    private long expiryTime;

    @DatabaseField(unique = true)
    private String securityToken;
    @DatabaseField()
    private String securityTokenAction;
    @DatabaseField()
    private long securityTokenExpiryTime;

    //Dao constructor
    public GoaspSession(){
    }

    //Data constructor
    public GoaspSession(GoaspUser user, long expiryInSeconds) {
        final long currentTime = System.currentTimeMillis();

        generateID();
        this.user = user;
        this.expiryTime = currentTime + (expiryInSeconds * 1000);
    }

    public void generateID() {
        byte[] idBytes = RandomManager.getBytes(RANDOM_SESSION_ID_LENGTH);

        this.id = Base64.getUrlEncoder().encodeToString(idBytes);
    }

    public void generateSecurityToken() {
        byte[] idBytes = RandomManager.getBytes(RANDOM_SESSION_ID_LENGTH);

        this.securityToken = Base64.getUrlEncoder().encodeToString(idBytes);
    }

    public void generateSecurityToken(String action, long expiryInSeconds) {
        final long currentTime = System.currentTimeMillis();

        generateSecurityToken();
        securityTokenAction = action;
        securityTokenExpiryTime = currentTime + (expiryInSeconds * 1000);
    }

    public String getId() {
        return id;
    }

    public GoaspUser getUser() {
        return user;
    }

    public void setUser(GoaspUser user) {
        this.user = user;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public String getSecurityTokenAction() {
        return securityTokenAction;
    }

    public long getSecurityTokenExpiryTime() {
        return securityTokenExpiryTime;
    }


    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public boolean isValid() {
        return !isExpired();
    }

    public boolean isSecurityTokenExpired() {
        //Security token is only valid if session is valid
        return System.currentTimeMillis() > securityTokenExpiryTime || isExpired();
    }

    public boolean isSecurityTokenValid(String action, String securityToken) {
        //Validate action
        if (action == null || !action.equals(this.securityTokenAction)) {
            return false;
        }

        //Validate token itself
        if (securityToken == null || !securityToken.equals(this.securityToken)) {
            return false;
        }

        //Validate expiration
        return !isSecurityTokenExpired();
    }
}
