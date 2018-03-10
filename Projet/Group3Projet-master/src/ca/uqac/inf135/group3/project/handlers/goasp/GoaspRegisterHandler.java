package ca.uqac.inf135.group3.project.handlers.goasp;

import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspUser;
import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.tools.database.SQLUniqueConstraintException;
import ca.uqac.inf135.group3.project.tools.html.HTML;
import ca.uqac.inf135.group3.project.tools.html.HTMLAttribute;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class GoaspRegisterHandler extends GoaspDatabaseHandler {
    //Username must consist of 3 to 30 alphanumeric characters, starting with an alphabetic character
    private static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9]{2,29}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final String PASSWORD_PATTERN = "^[A-Za-z0-9[,.+\\-*/$%!@]]{8,}$";

    public GoaspRegisterHandler(GoaspDatabase database) {
        super(database);
    }

    private HTML validateParameters(String username, String email, String confEmail, String password, String confPwd) {
        //Validate username
        if (username == null || !Pattern.matches(USERNAME_PATTERN, username)) {
            return HTML.div(
                    HTMLAttribute.classes("alert alert-danger"),
                    HTML.p("Invalid username"),
                    HTML.ul(
                            HTML.li("Username must consist of 3 to 30 alphanumeric characters, starting with an alphabetic character.")
                    )
            );
        }

        //Validate email match
        if (email != null && confEmail != null && !email.equals(confEmail)) {
            return HTML.div(
                    HTMLAttribute.classes("alert alert-danger"),
                    HTML.p("Email addresses do not match")
            );
        }

        //Validate email
        if (email == null || !Pattern.matches(EMAIL_PATTERN, email)) {
            return HTML.div(
                    HTMLAttribute.classes("alert alert-danger"),
                    HTML.p("Invalid email address"),
                    HTML.ul(
                            HTML.li("Email address must have a valid format like user@domain.xyz")
                    )
            );
        }

        //Validate password match
        if (password != null && confPwd != null && !password.equals(confPwd)) {
            return HTML.div(
                    HTMLAttribute.classes("alert alert-danger"),
                    HTML.p("Passwords do not match")
            );
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
        if (Pattern.matches(PASSWORD_PATTERN, password)) {
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
            return HTML.div(
                    HTMLAttribute.classes("alert alert-danger"),
                    HTML.p("Invalid password"),
                    HTML.ul(
                            HTML.li("Must be at least 8 characters long"),
                            HTML.li("Can only contain alphanumeric characters or these special characters: ,.+-*/$%!@"),
                            HTML.li("Must contain at least 1 capital letter"),
                            HTML.li("Must contain at least 1 lower case letter"),
                            HTML.li("Must contain at least 1 digit"),
                            HTML.li("Must contain at least 1 special character")
                    )
            );
        }

        //No error, all is valid
        return null;
    }

    @Override
    public void handle() throws IOException {
        final ExchangeHelper exchangeHelper = getExchangeHelper();

        //Get parameters
        final String goaspredirect = exchangeHelper.getParameter("goaspredirect");

        final String username;
        final String email;
        final String confEmail;
        final String password;
        final String confPwd;
        final boolean submit;
        if (exchangeHelper.getMethod() == HttpMethod.POST) {
            username = exchangeHelper.getParameter("username");
            email = exchangeHelper.getParameter("email");
            confEmail = exchangeHelper.getParameter("confemail");
            password = exchangeHelper.getParameter("password");
            confPwd = exchangeHelper.getParameter("confpwd");
            submit = exchangeHelper.getParameter("submit") != null;
        }
        else {
            username = null;
            email = null;
            confEmail = null;
            password = null;
            confPwd = null;
            submit = false;
        }

        String switch_url=String.format(
                "%s?goaspredirect=%s",
                GoaspConst.LOGIN_PATH,
                ExchangeHelper.escapeParameter(ExchangeHelper.escapeParameter(goaspredirect))
        );

        HTML error = null;

        if (submit) {
            //Validate submitted data
            error = validateParameters(username, email, confEmail, password, confPwd);

            if (error == null) {
                //Create a new user with these infos
                final GoaspUser user = new GoaspUser(username, password, email);

                try {
                    //Until unique constraint is COLLATE NOCASE, we'll check these constraints by ourselves
                    if (getDatabase().getUserByUsername(username) != null) {
                        throw new SQLUniqueConstraintException("user", "username");
                    }
                    if (getDatabase().getUserByEmail(email) != null) {
                        throw new SQLUniqueConstraintException("user", "email");
                    }

                    //NOTE: createUser will crash if username or email address already exists
                    getDatabase().createUser(user);

                    createSessionAndRedirect(user, goaspredirect);
                    return;
                } catch (SQLUniqueConstraintException e) {
                    if ("username".equals(e.getFieldName())) {
                        error = HTML.div(
                                HTMLAttribute.classes("alert alert-danger"),
                                HTML.p("Username already exist")
                        );
                    } else if ("email".equals(e.getFieldName())) {
                        error = HTML.div(
                                HTMLAttribute.classes("alert alert-danger"),
                                HTML.p("Email address already exist")
                        );
                    }
                }
                catch (SQLException e) {
                    error = HTML.div(
                            HTMLAttribute.classes("alert alert-danger"),
                            HTML.p("Unexpected error will registering user")
                    );
                }
            }
        }

        //Build login_controls sub page
        StspFile form = new StspFile("goasp/register_form.html");
        form.setHTML("submit_url", GoaspConst.REGISTER_PATH);
        form.setHTML("goaspredirect", goaspredirect);
        form.setHTML("username", username);
        form.setHTML("email", email);
        form.setHTML("confemail", confEmail);
        form.setHTML("switch_url", switch_url);
        form.setHTML("switch_label",  "Already have an account? Sign in");

        //Build register page
        StspFile page = new StspFile("goasp/base.html");
        page.setHTML("title", "GOASP - Register");
        page.setHTML("GOASP_APP_NAME", GoaspConst.APP_NAME);
        page.setHTML("error_message", error);
        page.setStsp("form", form);

        if (error == null) {
            exchangeHelper.ok(page);
        }
        else {
            exchangeHelper.badRequest(page);
        }
    }
}
