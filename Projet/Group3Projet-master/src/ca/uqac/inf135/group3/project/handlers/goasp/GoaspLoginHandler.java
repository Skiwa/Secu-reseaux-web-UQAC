package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.*;
import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.html.HTML;
import ca.uqac.inf135.group3.project.tools.html.HTMLAttribute;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;

import java.io.IOException;
import java.sql.SQLException;

public class GoaspLoginHandler extends GoaspDatabaseHandler {
    public GoaspLoginHandler(GoaspDatabase database) {
        super(database);
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        //Get parameters
        final String goaspredirect = exchangeHelper.getParameter("goaspredirect");
        final String username = exchangeHelper.getParameter("username");

        final String password;
        final boolean submit;
        if (exchangeHelper.getMethod() == HttpMethod.POST) {
            password = exchangeHelper.getParameter("password");
            submit = exchangeHelper.getParameter("submit") != null;
        } else {
            password = null;
            submit = false;
        }

        HTML error = null;

        if (submit) {
            if (username != null && password != null) {
                final GoaspUser user;
                try {
                    user = getDatabase().getUserByUsername(username);

                    if (user == null || !user.isValidPassword(password)) {
                        error = HTML.div(
                                HTMLAttribute.classes("alert alert-danger"),
                                HTML.p("Invalid credentials.")
                        );
                    }

                } catch (SQLException e) {
                    System.err.println("An error occurred fetching user by username on login");
                    e.printStackTrace();
                    exchangeHelper.internalError("searching for user");
                    return;
                }

                if (error == null) {
                    //Login successful, create a session
                    createSessionAndRedirect(user, goaspredirect);
                    return;
                }
            } else {
                error = HTML.div(
                        HTMLAttribute.classes("alert alert-danger"),
                        HTML.p("Username and password are mandatory")
                );
            }

        }

        String switch_url = String.format(
                "%s?goaspredirect=%s",
                GoaspConst.REGISTER_PATH,
                ExchangeHelper.escapeParameter(ExchangeHelper.escapeParameter(goaspredirect))
        );

        //Build login_form sub page
        StspFile form = new StspFile("goasp/login_form.html");
        form.setHTML("submit_url", GoaspConst.LOGIN_PATH);
        form.setHTML("goaspredirect", goaspredirect);
        if (username == null) {
            form.setHTML("username", "");
            form.setHTML("userFocus", "autofocus");
        }
        else {
            form.setHTML("username", username);
            form.setHTML("passFocus", "autofocus");
        }
        form.setHTML("switch_url", switch_url);
        form.setHTML("switch_label", "No account? Sign up");

        //Build login page
        StspFile page = new StspFile("goasp/base.html");
        page.setHTML("title", "GOASP - Sign in");
        page.setHTML("GOASP_APP_NAME", GoaspConst.APP_NAME);
        page.setHTML("error_message", error);
        page.setStsp("form", form);

        exchangeHelper.ok(page);
    }
}
