package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.TodoHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;

import java.io.IOException;
import java.sql.SQLException;

public class UpdateTodoHandler extends TodoHandler {

    public UpdateTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        //Validate posted data
        final String content = getContent(exchangeHelper);
        final Boolean done = getDone(exchangeHelper);

        if (content != null && done != null) {
            //Search for the TO-DO to update
            final Todo todo = getTodo(exchangeHelper);

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
