package ca.uqac.inf135.group3.project.model.entities.goasp;

import ca.uqac.inf135.group3.project.jwt.JwtToken;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "token")
public class GoaspToken {

    @DatabaseField(id = true)
    private String accessToken;
    @DatabaseField(foreign = true)
    private GoaspUser user;
    @DatabaseField(foreign = true)
    private GoaspSinglePageApp app;

    //NOTE: we're not keeping links to GoaspScope, but only a space separated scope name list
    @DatabaseField
    private String scope;

    @DatabaseField()
    private long expiryTime;

    //Dao constructor
    public GoaspToken() {
    }

    //Data constructor
    public GoaspToken(GoaspAuthCode authCode, long expiresInSeconds) {
        final long creationTime = System.currentTimeMillis();

        this.user = authCode.getUser();
        this.app = authCode.getApp();
        this.expiryTime = creationTime + (expiresInSeconds*1000);
        this.scope = authCode.getScope();

        //Create the payload
        final JSONObject payLoad = user.getJSON().add("expires", expiryTime);

        //Create the JWT Token
        JwtToken jwtToken = new JwtToken(payLoad, user.getSalt());

        this.accessToken = jwtToken.toString();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public GoaspUser getUser() {
        return user;
    }

    public GoaspSinglePageApp getApp() {
        return app;
    }

    public long getExpiryTime() {
        return expiryTime;
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

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public boolean isValid(String requestedScope) {
        //Check if token is valid for associated user
        JwtToken token = new JwtToken(accessToken);
        if (!token.isValid(user.getSalt())) {
            return false;
        }

        //Check if token has expired
        if (isExpired()) {
            return false;
        }

        //Check for scope grant
        String[] localScopes = GoaspScope.splitScopeString(scope);
        String[] requestedScopes = GoaspScope.splitScopeString(requestedScope);

        //Make sure each and every requested scope is present
        for (String req : requestedScopes) {
            boolean found = false;

            for (String local : localScopes) {
                if (local.equals(req)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }
}
