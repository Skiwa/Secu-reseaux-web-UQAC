package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class GetUserHandler extends DatabaseHandler {
    public GetUserHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        String path = exchangeHelper.getUriPath();

        //ID is stored right after /api/users/
        String strID = path.substring("/api/users/".length());

        final int id;
        try {
            id = Integer.parseInt(strID, 10);
        }
        catch (NumberFormatException e) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("message", "Invalid id")
                    .add("id", strID)
            );
            return;
        }

        final User user;
        try {
            user = getDatabase().selectUserByID(id);
        } catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("searching for user");
            return;
        }

        if (user == null) {
            exchangeHelper.gone("User", id);
            return;
        }

        //OK we found the user, return it
        exchangeHelper.ok(user.getJSON());
    }
}
