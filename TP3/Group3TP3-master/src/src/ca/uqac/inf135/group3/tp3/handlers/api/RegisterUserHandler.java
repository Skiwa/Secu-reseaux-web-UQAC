package ca.uqac.inf135.group3.tp3.handlers.api;

import ca.uqac.inf135.group3.tp3.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.tools.HttpMethod;
import ca.uqac.inf135.group3.tp3.tools.json.JSONArray;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterUserHandler extends DatabaseHandler {
    private final JSONObject loginTemplateJSON;

    public RegisterUserHandler(RESTodoDatabase database, JSONObject loginTemplateJSON) {
        super(database);
        this.loginTemplateJSON = loginTemplateJSON;
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        final JSONObject jsonValue = exchangeHelper.getJSON();
        //NOTE, jsonValue has already been validated, so it exist and it's expected keys too

        final String username = jsonValue.getString("username");
        final String password = jsonValue.getString("password");
        final String email = jsonValue.getString("mail");

        //Validate username
        //Username must consist of 3 to 30 alphanumeric characters, starting with an alphabetic character
        if (!Pattern.matches("^[a-zA-Z][a-zA-Z0-9]{2,29}$", username)) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("reason", "Invalid username")
                    .add("conditions", new JSONArray()
                            .add("Username must consist of 3 to 30 alphanumeric characters, starting with an alphabetic character.")
                    )
            );
            return;
        }

        //Validate password
        // Must be at least 8 characters long.
        // Can only contain alphanumeric characters or these special characters: ,.+-*/$%!@
        // Must contain at least 1 capital letter
        // Must contain at least 1 lower case letter
        // Must contain at least 1 digit
        // Must contain at least 1 special character

        //First test that all chars are valid
        boolean pwdOK = false;
        if (Pattern.matches("^[A-Za-z0-9[,.+\\-*/$%!@]]{8,}$", password)) {
            boolean hasCap = false;
            boolean hasLower = false;
            boolean hasDigit = false;
            boolean hasSpecial = false;
            final String allowedSpecial = ",.+-*/$%!@";

            for (char c : password.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    hasCap = true;
                }
                if (c >= 'a' && c <= 'z') {
                    hasLower = true;
                }
                if (c >= '0' && c <= '9') {
                    hasDigit = true;
                }
                if (allowedSpecial.indexOf(c) >= 0) {
                    hasSpecial = true;
                }
            }

            pwdOK = hasCap && hasLower && hasDigit && hasSpecial;
        }

        if (!pwdOK) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("reason", "Invalid password")
                    .add("conditions", new JSONArray()
                            .add("Must be at least 8 characters long")
                            .add("Can only contain alphanumeric characters or these special characters: ,.+-*/$%!@")
                            .add("Must contain at least 1 capital letter")
                            .add("Must contain at least 1 lower case letter")
                            .add("Must contain at least 1 digit")
                            .add("Must contain at least 1 special character")
                    )
            );
            return;
        }

        //Validate email
        if (!Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", email)) {
            exchangeHelper.badRequest(new JSONObject()
                    .add("reason", "Invalid email address")
                    .add("conditions", new JSONArray()
                            .add("Must be a valid email address")
                    )
            );
            return;
        }

        //Ok values are valid, now make sure username and email doesn't already exist
        try {
            if (getDatabase().selectUserByUsername(username) != null) {
                exchangeHelper.unauthorized(new JSONObject()
                        .add("reason", "Username already exist")
                        .add("conditions", new JSONArray()
                                .add("Username already exist")
                        )
                );
                return;
            }

            if (getDatabase().selectUserByEmail(email) != null) {
                exchangeHelper.unauthorized(new JSONObject()
                        .add("reason", "Email address already used by another user")
                        .add("conditions", new JSONArray()
                                .add("Email address already used by another user")
                        )
                );
                return;
            }
        } catch (SQLException e) {
            //Unable to validate if user exist, try to save it and let's see what happen
        }

        final User newUser;
        try {
            newUser = new User(username, password, email);

            getDatabase().insertUser(newUser);
        } catch (Exception e) {
            e.printStackTrace();
            exchangeHelper.internalError("creating user");
            return;
        }

        //Success
        exchangeHelper.created(new JSONObject()
                .add("status", "User created")
                .add("user", newUser.getJSON())
                .add("login", new JSONObject()
                        .add("message", "Use the following api, method and template to login")
                        .add("api", "/api/login")
                        .add("method", HttpMethod.POST)
                        .add("template", loginTemplateJSON)
                )
        );
    }
}
