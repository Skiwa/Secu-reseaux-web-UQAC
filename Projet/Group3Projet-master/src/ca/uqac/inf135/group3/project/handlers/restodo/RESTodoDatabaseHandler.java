package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public abstract class RESTodoDatabaseHandler extends DatabaseHandler {
    public RESTodoDatabaseHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    protected RESTodoDatabase getDatabase() {
        if (super.getDatabase() instanceof RESTodoDatabase) {
            return (RESTodoDatabase) super.getDatabase();
        }
        return null;
    }

    protected String getContent(ExchangeHelper exchangeHelper) throws IOException {
        final JSONObject jsonValue = exchangeHelper.getJSON();

        final String content = jsonValue.getString("content");

        //Make sure content and done are valid
        if (content != null && !"".equals(content)) {
            return content;
        }
        else {
            exchangeHelper.badRequest("'content' parameter must be a non-empty string.");
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
            exchangeHelper.badRequest("'done' parameter must be a true or false boolean value.");
            return null;
        }
    }

    protected Todo getTodoFromUri(ExchangeHelper exchangeHelper) throws IOException {
        final String userID = exchangeHelper.getUserID();

        String path = exchangeHelper.getUriPath();

        //ID is stored right after /api/todos/
        String strID = path.substring("/api/todos/".length());

        //Parse numeric ID
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

        //Find TO-DO in database
        final Todo todo;
        try {
            todo = getDatabase().getTodoByID(id);
        } catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("searching the todo");
            return null;
        }

        if (todo == null) {
            exchangeHelper.gone("Todo", id);
            return null;
        }

        if (userID == null || !userID.equals(todo.getUserID())) {
            exchangeHelper.unauthorized("Not allowed to access that resource.");
            return null;
        }

        return todo;
    }

}
