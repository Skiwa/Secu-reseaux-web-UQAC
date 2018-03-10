package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class CreateTODOHandler extends RESTodoDatabaseHandler {
    public CreateTODOHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        final String userID = exchangeHelper.getUserID();

        final String content = getContent(exchangeHelper);
        final Boolean done = getDone(exchangeHelper);

        if (content != null && done != null) {
            //Create TO-DO from data
            final Todo todo = new Todo(userID, content, done);

            //Save it
            try {
                getDatabase().createTodo(todo);
            } catch (SQLException e) {
                System.err.println("An error occurred creating TODO");
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
