package ca.uqac.inf135.group3.project.model.entities.goasp;

import ca.uqac.inf135.group3.project.goasp.GoaspApp;
import ca.uqac.inf135.group3.project.tools.crypto.RandomManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Base64;

@DatabaseTable(tableName = "spa")
public class GoaspSinglePageApp implements GoaspApp {
    private static final int RANDOM_CLIENT_ID_LENGTH = 64 / 4 * 3;    //We want a 64 bytes long base64 string by the end

    @DatabaseField(id = true)
    private String clientID;
    @DatabaseField(unique = true)
    private String name;
    @DatabaseField()
    private String callbackURL;


    public GoaspSinglePageApp() {
    }

    public GoaspSinglePageApp(String name, String callbackURL) {
        this.name = name;
        this.callbackURL = callbackURL;
        this.generateID();
    }

    public void generateID() {
        byte[] idBytes = RandomManager.getBytes(RANDOM_CLIENT_ID_LENGTH);

        this.clientID = Base64.getUrlEncoder().encodeToString(idBytes);
    }

    public void forceID(String id) {
        this.clientID = id;
    }

    @Override
    public String getPrefix() {
        //spa for Single Page App
        return "spa";
    }

    @Override
    public String getID() {
        return clientID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCallbackURL() {
        return callbackURL;
    }

    @Override
    public String getSecret() {
        return "";
    }
}
