package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.tp3.jwt.JwtToken;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class LoginHandler extends DatabaseHandler {

    public LoginHandler(RESTodoDatabase database) {
        super(database);
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final JSONObject jsonValue = exchangeHelper.getJSON();
        //NOTE, jsonValue has already been validated, so it exist and it's expected keys too

        //At this point, username and password are guarantied to exist, but their type might not be strings
        final String usernameOrEmail = jsonValue.getString("username");
        final String clearPassword = jsonValue.getString("password");
        if (usernameOrEmail == null) {
            exchangeHelper.badRequest("username property must be a string.");
            return;
        }
        if (clearPassword == null) {
            exchangeHelper.badRequest("password property must be a string.");
            return;
        }

        //Since "@" is not allowed in username and mandatory in email address, use it as discriminant
        final User user;
        try {
            //NOTE: no need for email or username validation since we're using prepared statement parameters
            //JDBC will format everything correctly and we'll be protected against SQL-injection
            //Furthermore, even if the user or email format is invalid, we'll simply match no user.
            if (usernameOrEmail.indexOf('@') >= 0) {
                //It's an Email address
                user = getDatabase().selectUserByEmail(usernameOrEmail);
            } else {
                //It's a username
                user = getDatabase().selectUserByUsername(usernameOrEmail);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            exchangeHelper.internalError("searching the user");
            return;
        }

        if (user != null) {
            if (user.isValidPassword(clearPassword)) {
                //Create token payload
                JSONObject jsonPayload = new JSONObject()
                        .add("id", user.getId())
                        .add("username", user.getUsername())
                        .add("email", user.getEmail());

                //Create JWT token
                JwtToken token = new JwtToken(jsonPayload, user.getSalt());

                //Respond by an OK providing token as response
                exchangeHelper.ok(new JSONObject().add("token", token));

                return;
            }
        }

        //If we reached this point, credentials are invalid
        exchangeHelper.unauthorized("Invalid credentials");
    }
}
