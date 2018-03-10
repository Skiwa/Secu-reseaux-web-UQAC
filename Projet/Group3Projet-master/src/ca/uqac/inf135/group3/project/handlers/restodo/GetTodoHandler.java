package ca.uqac.inf135.group3.project.handlers.restodo;

import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;

import java.io.IOException;

public class GetTodoHandler extends RESTodoDatabaseHandler {

    public GetTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();
        final Todo todo = getTodoFromUri(exchangeHelper);

        if (todo != null) {
            //OK we found the TO-DO, return it
            exchangeHelper.ok(todo.getJSON());
        }
        //Else, a response has already been sent
    }
}
