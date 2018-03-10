package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.*;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.html.HTML;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspRequestHandler extends GoaspDatabaseHandler {

    public GoaspRequestHandler(GoaspDatabase database) {
        super(database);
    }

    private void redirectError(String reason) {
        final String redirectURI = getExchangeHelper().getParameter("redirect_uri");
        getExchangeHelper().redirectTo(String.format("%s?error=%s", redirectURI, ExchangeHelper.escapeParameter(reason)));
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();
        final GoaspSession session = getExchangeHelper().getSession();

        //Get parameters
        final String responseType = exchangeHelper.getParameter("response_type");
        final String clientID = exchangeHelper.getParameter("client_id");
        final String redirectURI = exchangeHelper.getParameter("redirect_uri");
        final String scopeString = exchangeHelper.getParameter("scope");
        final String state = exchangeHelper.getParameter("state");

        final String goaspredirect;
        if (exchangeHelper.getMethod() == HttpMethod.GET) {
            //goaspredirect is the current URL
            goaspredirect = String.format(
                    "%s?%s",
                    GoaspConst.REQUEST_PATH,
                    getQueryString()
            );
        }
        else {
            //Otherwise read it from submitted data
            goaspredirect = exchangeHelper.getParameter("goaspredirect");
        }

        if ("code".equals((responseType != null ? responseType : "").toLowerCase())) {

            //Search for the clientID
            GoaspSinglePageApp app;
            try {
                app = getDatabase().getSpaByID(clientID);
            } catch (SQLException e) {
                System.err.println("An redirectError occurred searching for application");
                e.printStackTrace();
                redirectError("server_error");
                return;
            }

            if (app != null) {
                final String appName = app.getName();

                //Validate scopes while building permissions
                HTML permissions = HTML.ul();
                try {
                    for (String scopeName : GoaspScope.splitScopeString(scopeString)) {
                        final GoaspScope scope = getDatabase().getScopeByName(scopeName);

                        if (scope != null && (scope.isPublic() || scope.getApp().getID().equals(app.getID()))) {
                            permissions.add(HTML.li(ExchangeHelper.escapeHTML(scope.getDescription())));
                        }
                        else {
                            redirectError("invalid_scope");
                            return;
                        }
                    }
                }
                catch (SQLException e) {
                    System.err.println("An redirectError occurred searching for a scope");
                    e.printStackTrace();
                    redirectError("server_error");
                    return;
                }

                final String submit;
                if (exchangeHelper.getMethod() == HttpMethod.POST) {
                    submit = exchangeHelper.getParameter("submit");
                } else {
                    submit = null;
                }

                if (submit != null) {
                    if ("approve".equals(submit)) {
                        //Create an authorization code
                        GoaspAuthCode auth = new GoaspAuthCode(session.getUser(), app, redirectURI, scopeString);

                        try {
                            getDatabase().createAuth(auth);
                        } catch (SQLException e) {
                            System.err.println("An error occurred creating an authorization code");
                            e.printStackTrace();
                            redirectError("server_error");
                            return;
                        }

                        final String authCode = auth.getCode();

                        exchangeHelper.redirectTo(String.format(
                                "%s?code=%s&state=%s",
                                redirectURI,
                                ExchangeHelper.escapeParameter(authCode),
                                ExchangeHelper.escapeParameter(state)
                        ));
                        return;
                    }
                    else {
                        //Declined
                        redirectError("access_denied");
                        return;
                    }
                }

                //Generate a security token
                session.generateSecurityToken(GoaspConst.REQUEST_PATH, 10*60); //Allow 10 minutes to decide if he should accept or not, seems enough

                try {
                    getDatabase().updateSession(session);
                } catch (SQLException e) {
                    System.err.println("An error occurred saving generated a security token");
                    e.printStackTrace();
                    redirectError("server_error");
                    return;
                }

                String logout_url = String.format(
                        "%s?goaspredirect=%s",
                        GoaspConst.LOGOUT_PATH,
                        ExchangeHelper.escapeParameter(ExchangeHelper.escapeParameter(goaspredirect))
                );

                //Build request_form sub page
                StspFile form = new StspFile("goasp/request_form.html");
                form.setHTML("app_name", appName);
                form.setHTML("permissions", permissions);
                form.setHTML("submit_url", GoaspConst.REQUEST_PATH);
                form.setHTML("response_type", responseType);
                form.setHTML("client_id", clientID);
                form.setHTML("redirect_uri", redirectURI);
                form.setHTML("scope", scopeString);
                form.setHTML("state", state);
                form.setHTML("securityToken", session.getSecurityToken());
                form.setHTML("goaspredirect", goaspredirect);
                form.setHTML("switch_url", logout_url);
                form.setHTML("switch_label", "Disconnect and log with another account.");

                String usernameEmail = String.format(
                        "%s (%s)",
                        session.getUser().getUsername(),
                        session.getUser().getEmail()
                );

                //Build request page
                StspFile page = new StspFile("goasp/base.html");
                page.setHTML("title", "GOASP - Permission requested");
                page.setHTML("GOASP_APP_NAME", GoaspConst.APP_NAME);
                page.setHTML("username (email)", usernameEmail);
                page.setHTML("error_message", "");
                page.setStsp("form", form);

                exchangeHelper.ok(page);

            } else {
                redirectError("unauthorized_client");
            }
        }
        else {
            redirectError("unsupported_response_type");
        }
    }
}
