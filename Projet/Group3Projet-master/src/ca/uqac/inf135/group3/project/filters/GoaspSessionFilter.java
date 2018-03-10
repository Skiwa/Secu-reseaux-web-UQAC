package ca.uqac.inf135.group3.project.filters;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspSession;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteFilter;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspSessionFilter implements RouteFilter {

    final GoaspDatabase database;
    final String securityTokenAction;
    final boolean sessionRequired;

    public GoaspSessionFilter(GoaspDatabase database, String securityTokenAction, boolean sessionRequired) {
        this.database = database;
        this.securityTokenAction = securityTokenAction;
        this.sessionRequired = sessionRequired;
    }
    public GoaspSessionFilter(GoaspDatabase database, String securityTokenAction) {
        this(database, securityTokenAction, true);
    }
    public GoaspSessionFilter(GoaspDatabase database, boolean sessionRequired) {
        this(database, null, sessionRequired);
    }
    public GoaspSessionFilter(GoaspDatabase database) {
        this(database, null);
    }

    @Override
    public boolean doFilter(ExchangeHelper exchangeHelper) throws IOException {
        String sessionID = exchangeHelper.getCookie(GoaspConst.SESSION_COOKIE_NAME);

        if (sessionID != null) {
            //There's a session in cookie

            //Search for it in database
            GoaspSession session;
            try {
                session = database.getSessionByID(sessionID);
            } catch (SQLException e) {
                session = null;
                System.err.println("An error occurred searching for session:" + sessionID);
                e.printStackTrace();
            }

            exchangeHelper.putValue("session", session);

            if (sessionRequired) {
                if (session != null && !session.isExpired()) {
                    String securityToken = exchangeHelper.getParameter("securityToken");

                    //We don't want to validate security token or the security token is valid
                    if (securityTokenAction == null || session.isSecurityTokenValid(securityTokenAction, securityToken)) {
                        //OK, continue with requested route (or next filter)
                        return true;
                    }
                }
            }
            else {
                //Session was not required, we just wanted to make the getSession available
                return true;
            }
        }

        //Redirect to login with same query string
        String fullRedirect = String.format(
                "%s?goaspredirect=%s",
                GoaspConst.LOGIN_PATH,
                ExchangeHelper.escapeParameter(ExchangeHelper.escapeParameter(
                        String.format(
                                "%s?%s",
                                exchangeHelper.getUriPath(),
                                exchangeHelper.getRequestQuery()
                        )
                ))
        );

        exchangeHelper.redirectTo(fullRedirect);
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
