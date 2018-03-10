package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONArray;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListTodosHandler extends RESTodoDatabaseHandler {

    public ListTodosHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();
        final String userID = exchangeHelper.getUserID();

        //Fetch all user's todos
        try {
            List<Todo> allTodos = getDatabase().getAllUserTodos(userID);
            JSONArray todoArray = new JSONArray();

            for (Todo todo: allTodos) {
                todoArray.add(todo.getJSON());
            }

            exchangeHelper.ok(todoArray);
        } catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("fetching user's todos");
        }
    }
}
