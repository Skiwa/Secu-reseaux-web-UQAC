package ca.uqac.inf135.group3.project.model.entities.restodo;

import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "todo")
public class Todo {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true)
    private String userID;

    @DatabaseField(canBeNull = false)
    private String content;

    @DatabaseField
    private boolean done;

    //DAO constructor
    public Todo() {
    }

    //New user creation constructor
    public Todo(String userID, String content, boolean done) {
        this.userID = userID;
        this.content = content;
        this.done = done;
    }

    //Public API
    public JSONObject getJSON() {
        return new JSONObject()
                .add("id", id)
                .add("content", content)
                .add("done", done);
    }

    public int getId() {
        return id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
