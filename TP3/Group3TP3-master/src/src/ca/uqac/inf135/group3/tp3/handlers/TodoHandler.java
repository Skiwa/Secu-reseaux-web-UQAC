package ca.uqac.inf135.group3.tp3.handlers;

import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public abstract class TodoHandler extends DatabaseHandler {
    public TodoHandler(RESTodoDatabase database) {
        super(database);
    }

    protected String getContent(ExchangeHelper exchangeHelper) throws IOException {
        final JSONObject jsonValue = exchangeHelper.getJSON();

        final String content = jsonValue.getString("content");

        //Make sure content and done are valid
        if (content != null && !"".equals(content)) {
            return content;
        }
        else {
            exchangeHelper.badRequest("'content' parameter must not be a not-null string.");
            return null;
        }
    }

    protected Boolean getDone(ExchangeHelper exchangeHelper) throws IOException {
        final JSONObject jsonValue = exchangeHelper.getJSON();
        final Object doneObj = jsonValue.get("done");

        if (doneObj != null && doneObj instanceof Boolean) {
            return (Boolean) doneObj;
        }
        else {
            exchangeHelper.badRequest("'done' parameter must be a boolean true or false.");
            return null;
        }
    }

    protected Todo getTodo(ExchangeHelper exchangeHelper) throws IOException {
        final User user = exchangeHelper.getUser();

        String path = exchangeHelper.getUriPath();

        //ID is stored right after /api/todos/
        String strID = path.substring("/api/todos/".length());

        final int id;
        try {
            id = Integer.parseInt(strID, 10);
        }
        catch (NumberFormatException e) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("message", "Invalid id")
                    .add("id", strID)
            );
            return null;
        }

        final Todo todo;
        try {
            todo = getDatabase().selectTodoByID(id);
        } catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("searching the user");
            return null;
        }

        if (todo == null) {
            exchangeHelper.gone("Todo", id);
            return null;
        }

        if (todo.getUserID() != user.getId()) {
            exchangeHelper.unauthorized("Not allowed to access that resource.");
            return null;
        }

        return todo;
    }
}
