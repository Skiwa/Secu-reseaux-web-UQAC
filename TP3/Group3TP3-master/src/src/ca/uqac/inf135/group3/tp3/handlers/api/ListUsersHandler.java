package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.tools.json.JSONArray;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListUsersHandler extends DatabaseHandler {

    public ListUsersHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {

        //Fetch all users
        try {
            List<User> allUsers = getDatabase().selectAllUsers();
            JSONArray userArray = new JSONArray();

            for (User user : allUsers) {
                userArray.add(user.getJSON());
            }

            exchangeHelper.ok(userArray);
        } catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("fetching users");
        }

    }
}
