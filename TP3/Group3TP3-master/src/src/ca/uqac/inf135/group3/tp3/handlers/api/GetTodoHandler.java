package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.TodoHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;

import java.io.IOException;

public class GetTodoHandler extends TodoHandler {

    public GetTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final Todo todo = getTodo(exchangeHelper);

        if (todo != null) {
            //OK we found the TO-DO, return it
            exchangeHelper.ok(todo.getJSON());
        }
        //Else, a response has already been sent
    }
}
