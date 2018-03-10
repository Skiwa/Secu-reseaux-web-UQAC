package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class DeleteTodoHandler extends RESTodoDatabaseHandler {

    public DeleteTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();
        final Todo todo = getTodoFromUri(exchangeHelper);

        if (todo != null) {
            //OK we found the TO-DO, delete it
            try {
                getDatabase().deleteTodo(todo);
            } catch (SQLException e) {
                e.printStackTrace();
                exchangeHelper.internalError("deleting todo");
                return;
            }

            //Confirm deletion
            exchangeHelper.ok(new JSONObject().add("status", "TODO deleted"));
        }
        //Else, a response has already been sent
    }
}
