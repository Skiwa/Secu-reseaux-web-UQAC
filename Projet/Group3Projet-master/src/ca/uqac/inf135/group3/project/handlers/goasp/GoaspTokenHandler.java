package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspAuthCode;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspToken;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspTokenHandler extends GoaspDatabaseHandler {
    public GoaspTokenHandler(GoaspDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        //Get parameters
        final String grantType = exchangeHelper.getParameter("grant_type");
        final String code = exchangeHelper.getParameter("code");
        final String redirectURI = exchangeHelper.getParameter("redirect_uri");
        final String clientID = exchangeHelper.getParameter("client_id");

        JSONObject jsonError = new JSONObject();

        if ("authorization_code".equals((grantType != null ? grantType : "").toLowerCase())) {

            //Search for authorization code
            final GoaspAuthCode authCode;
            try {
                authCode = getDatabase().getAuthByCode(code);
            } catch (SQLException e) {
                System.err.println("An error occurred searching for authorization code");
                e.printStackTrace();
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }

            if (authCode == null) {
                System.err.println("authorization code could not be found");
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }
            if (authCode.isExpired()) {
                System.err.println("authorization code has expired");
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }

            //Validate client ID
            if (clientID == null || !clientID.equals(authCode.getApp().getID())) {
                System.err.println("client_id doesn't match authorization code's client ID");
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }

            //Validate redirect URI
            if (redirectURI == null || !redirectURI.equals(authCode.getRedirectURI())) {
                System.err.println("redirect_uri doesn't match authorization code's redirect URI");
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }

            //Everything is fine, let's build the token
            final int expiresIn = 60*60; //Default to 1h expiration
            GoaspToken token = new GoaspToken(authCode, expiresIn);

            //Save it to database
            try {
                getDatabase().createToken(token);
            } catch (SQLException e) {
                System.err.println("An error occurred saving token");
                e.printStackTrace();
                jsonError.add("error", "invalid_grant");
                exchangeHelper.badRequest(jsonError, true);
                return;
            }

            //We no longer need the authCode
            try {
                getDatabase().deleteAuth(authCode);
            } catch (SQLException e) {
                //We don't care if it fail
            }

            //{"access_token":"ACCESS_TOKEN","token_type":"bearer","expires_in":2592000,"refresh_token":"REFRESH_TOKEN","scope":"read","uid":100101,"info":{"name":"Mark E. Mark","email":"mark@thefunkybunch.com"}}
            exchangeHelper.setCacheControlNoStore();
            exchangeHelper.ok(new JSONObject()
                    .add("access_token", token.getAccessToken())
                    .add("token_type", "bearer")
                    .add("expires_in", expiresIn)
            );
        }
        else {
            jsonError.add("error", "unsupported_grant_type");
            exchangeHelper.badRequest(jsonError, true);
        }
    }
}
