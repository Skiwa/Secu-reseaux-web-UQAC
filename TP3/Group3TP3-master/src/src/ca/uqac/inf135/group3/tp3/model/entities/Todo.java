package ca.uqac.inf135.group3.tp3.model.entities;

import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

public class Todo {
    private int id;
    private final int userID;
    private String content;
    private boolean done;

    //Database data constructor (fields as stored)
    public Todo(int id, int userID, String content, boolean done) {
        this.id = id;
        this.userID = userID;
        this.content = content;
        this.done = done;
    }

    //New user creation constructor
    public Todo(User owner, String content, boolean done) {
        this(0, owner.getId(), content, done);
    }

    //Public API
    public JSONObject getJSON() {
        return new JSONObject()
                .add("id", id)
                .add("content", content)
                .add("done", done);
    }

    //Default Getter and Setters
    public int getId() {
        return id;
    }

    public int getUserID() {
        return userID;
    }

    public void setId(int id) {
        this.id = id;
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
