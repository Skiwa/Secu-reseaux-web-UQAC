package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.TodoHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.json.JSONArray;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListTodosHandler extends TodoHandler {

    public ListTodosHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final User user = exchangeHelper.getUser();

        //Fetch all user's todos
        try {
            List<Todo> allTodos = getDatabase().selectAllUserTodos(user);
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
