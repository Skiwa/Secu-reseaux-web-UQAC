package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.handlers.DatabaseHandler;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspSession;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspUser;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;

import java.sql.SQLException;

public abstract class GoaspDatabaseHandler extends DatabaseHandler {
    public GoaspDatabaseHandler(GoaspDatabase database) {
        super(database);
    }

    @Override
    protected GoaspDatabase getDatabase() {
        if (super.getDatabase() instanceof GoaspDatabase) {
            return (GoaspDatabase) super.getDatabase();
        }
        return null;
    }

    protected String getQueryString() {
        if (getExchangeHelper().getMethod() == HttpMethod.GET) {
            return getExchangeHelper().getRequestQuery();
        }
        else {
            return getExchangeHelper().getParameter("queryString");
        }
    }

    protected void createSessionAndRedirect(GoaspUser user, String redirect) {
        int sessionDurationSeconds = 24*60*60; //Make sessions valid for 24h
        GoaspSession session = new GoaspSession(user, sessionDurationSeconds);
        try {
            getDatabase().createSession(session);
        } catch (SQLException e) {
            System.err.println("An error occurred creating a session");
            e.printStackTrace();
            getExchangeHelper().internalError("creating session");
            return;
        }

        //Set the session cookie
        getExchangeHelper().setCookie(GoaspConst.SESSION_COOKIE_NAME, session.getId(), GoaspConst.BASE_PATH, sessionDurationSeconds, true, true);
        //And get back to the originally requested page
        getExchangeHelper().redirectTo(redirect);

    }
}
