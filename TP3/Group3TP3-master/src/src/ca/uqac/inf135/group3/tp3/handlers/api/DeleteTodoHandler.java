package ca.uqac.inf135.group3.tp3.handlers.api;

        import ca.uqac.inf135.group3.tp3.handlers.TodoHandler;
        import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
        import ca.uqac.inf135.group3.tp3.model.entities.Todo;
        import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
        import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

        import java.io.IOException;
        import java.sql.SQLException;

public class DeleteTodoHandler extends TodoHandler {

    public DeleteTodoHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final Todo todo = getTodo(exchangeHelper);

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
