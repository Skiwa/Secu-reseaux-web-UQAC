package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;

import java.io.IOException;
import java.sql.SQLException;

public class UpdateTodoHandler extends RESTodoDatabaseHandler {

    public UpdateTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();
        final String userID = exchangeHelper.getUserID();

        final String content = exchangeHelper.getJSON().getString("content");
        final Boolean done = exchangeHelper.getJSON().getBoolean("done");

        //Validate posted data
        if (content != null && done != null) {
            //Search for the TO-DO to update
            final Todo todo = getTodoFromUri(exchangeHelper);

            if (todo != null) {
                //OK we found the TO-DO, update it, and save it
                todo.setContent(content);
                todo.setDone(done);

                try {
                    getDatabase().updateTodo(todo);
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchangeHelper.internalError("updating todo");
                    return;
                }

                //Return the updated TO-DO
                exchangeHelper.ok(todo.getJSON());
            }
        }
        //Else, a response has already been sent
    }
}
