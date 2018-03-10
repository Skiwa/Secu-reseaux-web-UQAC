package ca.uqac.inf135.group3.project.model.entities.goasp;

import ca.uqac.inf135.group3.project.tools.crypto.RandomManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Base64;

@DatabaseTable(tableName = "authcode")
public class GoaspAuthCode {
    private static final int RANDOM_AUTH_CODE_LENGTH = 64 / 4 * 3;    //We want a 64 bytes long base64 string by the end

    @DatabaseField(id = true)
    private String code;
    @DatabaseField(foreign = true)
    private GoaspUser user;
    @DatabaseField(foreign = true)
    private GoaspSinglePageApp app;
    @DatabaseField()
    private String redirectURI;
    @DatabaseField()
    private String scope;

    @DatabaseField()
    private long expiryTime;

    //DAO constructor
    public GoaspAuthCode() {
    }

    //New Authorization constructor
    public GoaspAuthCode(GoaspUser user, GoaspSinglePageApp app, String redirectURI, String scope) {
        final long creationTime = System.currentTimeMillis();

        this.user = user;
        this.app = app;
        this.redirectURI = redirectURI;
        this.scope = scope;
        this.expiryTime = creationTime + (5 * 60 * 1000); //Auth code will be valid for 5 minutes, but that's way enough for a valid connection

        generateCode();
    }

    public void generateCode() {
        byte[] idBytes = RandomManager.getBytes(RANDOM_AUTH_CODE_LENGTH);

        this.code = Base64.getUrlEncoder().encodeToString(idBytes);
    }

    public String getCode() {
        return code;
    }

    public GoaspUser getUser() {
        return user;
    }

    public GoaspSinglePageApp getApp() {
        return app;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public String getScope() {
        return scope;
    }

    public void setUser(GoaspUser user) {
        this.user = user;
    }

    public void setApp(GoaspSinglePageApp app) {
        this.app = app;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.expiryTime;
    }
}
