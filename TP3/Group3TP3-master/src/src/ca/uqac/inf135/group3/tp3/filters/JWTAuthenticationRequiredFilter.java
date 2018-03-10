package ca.uqac.inf135.group3.tp3.filters;

import ca.uqac.inf135.group3.tp3.jwt.JwtToken;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.tp3.pipeline.RouteFilter;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class JWTAuthenticationRequiredFilter implements RouteFilter {
    private final RESTodoDatabase database;

    public JWTAuthenticationRequiredFilter(RESTodoDatabase database) {
        this.database = database;
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        final String auth = exchangeHelper.getAuthorization();

        if (auth != null) {
            //split the authentication string
            final String[] parts = auth.split(" ");

            //We expect 2 strings ("Bearer" and the token)
            if (parts.length == 2) {
                if ("BEARER".equals(parts[0].toUpperCase())) {
                    //Create a JwtToken from the string
                    final JwtToken token = new JwtToken(parts[1]);

                    //Make sure the token has a valid payload containing the user id
                    final JSONObject payload = token.getJSONPayload();

                    if (payload != null) {
                        final int userID = payload.getInt("id");

                        //Find user
                        try {
                            final User user = database.selectUserByID(userID);

                            if (user != null) {
                                //Make sure the token is valid
                                if (token.isValid(user.getSalt())) {
                                    //We are authenticated
                                    //Register the user
                                    exchangeHelper.putValue("user", user);
                                    //Continue with the request
                                    return true;
                                }
                            }
                        }
                        catch (SQLException e) {
                            //Let the default unauthorized behaviour handle it
                        }
                    }
                }
            }
        }

        //No authenticated user
        exchangeHelper.unauthorized();
        //Do NOT continue
        return false;
    }
}
