package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspSession;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspLogoutHandler extends GoaspDatabaseHandler {

    public GoaspLogoutHandler(GoaspDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        //Read parameters
        final String goaspredirect = exchangeHelper.getParameter("goaspredirect");

        GoaspSession session = exchangeHelper.getSession();

        if (session != null) {
            try {
                getDatabase().deleteSession(session);
            } catch (SQLException e) {
                System.err.println("An error occurred while deleting session");
                e.printStackTrace();
                exchangeHelper.internalError("deleting session");
                return;
            }

            //Clear cookie
            exchangeHelper.setCookie(GoaspConst.SESSION_COOKIE_NAME, "", GoaspConst.BASE_PATH, 0, true, true);
        }

        //Redirect to login
        exchangeHelper.redirectTo(String.format(
                "%s?goaspredirect=%s",
                GoaspConst.LOGIN_PATH,
                ExchangeHelper.escapeParameter(ExchangeHelper.escapeParameter(goaspredirect))
        ));
    }
}
