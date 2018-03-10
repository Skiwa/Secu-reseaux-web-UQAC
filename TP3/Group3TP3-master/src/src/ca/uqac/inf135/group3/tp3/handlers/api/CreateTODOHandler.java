package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.TodoHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class CreateTODOHandler extends TodoHandler {
    public CreateTODOHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final String content = getContent(exchangeHelper);
        final Boolean done = getDone(exchangeHelper);

        if (content != null && done != null) {
            final User user = exchangeHelper.getUser();

            //Create TO-DO
            final Todo todo = new Todo(user, content, done);

            //Save it
            try {
                getDatabase().insertTodo(todo);
            } catch (SQLException e) {
                e.printStackTrace();
                exchangeHelper.internalError("creating Todo");
                return;
            }

            //Success
            exchangeHelper.created(new JSONObject()
                    .add("status", "Todo created")
                    .add("todo", todo.getJSON())
            );
        }
    }
}
