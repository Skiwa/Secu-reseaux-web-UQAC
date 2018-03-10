package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspToken;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspValidateToken extends GoaspDatabaseHandler {
    public GoaspValidateToken(GoaspDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        //Read parameters
        String tokenParam = exchangeHelper.getParameter("token");
        String scopeParam = exchangeHelper.getParameter("scope");

        final GoaspToken token;
        try {
            token = getDatabase().getToken(tokenParam);
        } catch (SQLException e) {
            System.err.println("Error searching for token in ValidateToken handler");
            e.printStackTrace();
            exchangeHelper.internalError("searching for token");
            return;
        }

        JSONObject response = new JSONObject().add("token", tokenParam);
        if (token != null && token.isValid(scopeParam)) {
            response.add("status", "valid");
            exchangeHelper.ok(response);
        }
        else {
            response.add("status", "invalid");
            exchangeHelper.forbidden(response, true);
        }
    }
}
