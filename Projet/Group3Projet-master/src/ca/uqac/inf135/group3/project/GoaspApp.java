package ca.uqac.inf135.group3.project;

import ca.uqac.inf135.group3.project.filters.GoaspSessionFilter;
import ca.uqac.inf135.group3.project.filters.RequiredRequestParametersFilter;
import ca.uqac.inf135.group3.project.goasp.GoaspConst;
import ca.uqac.inf135.group3.project.handlers.goasp.*;
import ca.uqac.inf135.group3.project.model.database.GoaspDatabase;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspScope;
import ca.uqac.inf135.group3.project.model.entities.goasp.GoaspSinglePageApp;
import ca.uqac.inf135.group3.project.pipeline.RouteServer;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;
import ca.uqac.inf135.group3.project.tools.stsp.StspFile;
import ca.uqac.inf135.group3.project.tools.stsp.StspTag;

import java.sql.SQLException;
import java.util.List;

public class GoaspApp extends WebApp {
    private static final int DEFAULT_PORT = 8081;
    private static final String APP_NAME = "GOASP";

    public static void main (String[] args) {
        new GoaspApp(args)
                .start();
    }

    private GoaspDatabase database;

    protected GoaspApp(String[] args) {
        super(args, APP_NAME);
    }

    @Override
    protected int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected SQLiteHelper getDatabase() throws SQLException {
        database = new GoaspDatabase(false);
        return database;
    }

    @Override
    protected void doPostLoadDatabaseTasks() {
        //Register RESTodo FrontEnd application if not already

        //NOTE: Usually, the client ID would be automatically generated upon registration but since in our case the
        // app is already deployed with an hard coded client ID, we have to hard code it here too
        final String REG_APP_NAME = "RESTodo FrontEnd";
        final String REG_APP_CALLBACK_URL = "https://group3.stremblay.com/client/";
        final String REG_APP_CLIENT_ID = "ytuCRsoQmqTVsGzsaXo818p0TPq1lElI_-3iPgZcR2pXnJtaBds_wtF1wu53MswZ";

        System.out.println(String.format("Registering %s app if not already registered....", REG_APP_NAME));

        GoaspSinglePageApp app;
        try {
            app = database.getSpaByName(REG_APP_NAME);
        } catch (SQLException e) {
            System.err.println("An error occurred searching for GOAPS application " + REG_APP_NAME);
            e.printStackTrace();
            app = null;
        }

        if (app == null) {
            System.out.println("App not registered, registering it with hardcoded id: " + REG_APP_CLIENT_ID);
            System.out.println("Let's assume the app was registered with GOAPS before ID was put in client app.");
            app = new GoaspSinglePageApp(REG_APP_NAME, REG_APP_CALLBACK_URL);
            app.forceID(REG_APP_CLIENT_ID);

            try {
                database.createSpa(app);

                //Create application "scopes"
                database.createScope(new GoaspScope("restodo_read", app, "Read TODOs"));
                database.createScope(new GoaspScope("restodo_add", app, "Create TODOs"));
                database.createScope(new GoaspScope("restodo_edit", app, "Update TODOs"));
                database.createScope(new GoaspScope("restodo_del", app, "Delete TODOs"));

                System.out.println("App registered successfully");
            } catch (SQLException e) {
                System.err.println("An error occurred registering GOASP application " + REG_APP_NAME);
                e.printStackTrace();
            }
        }
        else {
            System.out.println("App already registered.");
        }
        System.out.println();

    }

    @Override
    protected void registerCustomRoutes(RouteServer server) {
        server.addRoute(HttpMethod.GET, GoaspConst.LOGIN_PATH, new GoaspLoginHandler(database))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.LOGIN_REQUIRED_PARAMETERS))
        ;
        server.addRoute(HttpMethod.POST, GoaspConst.LOGIN_PATH, new GoaspLoginHandler(database))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.LOGIN_REQUIRED_PARAMETERS)
                        .addRequired(GoaspConst.LOGIN_POST_ADITIONNAL_REQUIRED_PARAMETERS)
                )
        ;
        server.addRoute(HttpMethod.GET, GoaspConst.LOGOUT_PATH, new GoaspLogoutHandler(database))
                .addPreFilter(new GoaspSessionFilter(database, false))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.LOGOUT_REQUIRED_PARAMETERS))
        ;
        server.addRoute(HttpMethod.GET, GoaspConst.REQUEST_PATH, new GoaspRequestHandler(database))
                .addPreFilter(new GoaspSessionFilter(database))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.REQUEST_REQUIRED_PARAMETERS))
        ;
        server.addRoute(HttpMethod.POST, GoaspConst.REQUEST_PATH, new GoaspRequestHandler(database))
                .addPreFilter(new GoaspSessionFilter(database, GoaspConst.REQUEST_PATH))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.REQUEST_REQUIRED_PARAMETERS)
                        .addRequired(GoaspConst.REQUEST_POST_ADITIONNAL_REQUIRED_PARAMETERS)
                )
        ;
        server.addRoute(HttpMethod.GET, GoaspConst.REGISTER_PATH, new GoaspRegisterHandler(database))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.REGISTER_REQUIRED_PARAMETERS))
        ;
        server.addRoute(HttpMethod.POST, GoaspConst.REGISTER_PATH, new GoaspRegisterHandler(database))
                .addPreFilter(new RequiredRequestParametersFilter(GoaspConst.REGISTER_REQUIRED_PARAMETERS)
                        .addRequired(GoaspConst.REGISTER_POST_ADITIONNAL_REQUIRED_PARAMETERS)
                )
        ;
        server.addRoute(HttpMethod.POST, GoaspConst.TOKEN_PATH, new GoaspTokenHandler(database))
                .addPreFilter(
                        new RequiredRequestParametersFilter(GoaspConst.TOKEN_REQUIRED_PARAMETERS)
                                .setErrorReply(new JSONObject().add("error", "invalid_request"))
                )
        ;
        server.addRoute(HttpMethod.POST, GoaspConst.VALIDATE_TOKEN_PATH, new GoaspValidateToken(database))
                .addPreFilter(
                        new RequiredRequestParametersFilter(GoaspConst.VALIDATE_TOKEN_REQUIRED_PARAMETERS)
                )
        ;
    }

    private void testStspTemplate(String path) {
        StspFile stspFile = new StspFile(path);

        if ("".equals(stspFile)) {
            System.err.println(String.format("*** ERROR : Template '%s' was not found or is empty ***"));
        }
        else {
            List<StspTag> tags = stspFile.getTagList();

            if (tags.size() > 0) {
                System.out.println(String.format("Template '%s' is OK.", path));
            }
            else {
                System.err.println(String.format("*** ERROR : Template '%s' should have at least 1 tag ***"));
            }
        }
    }

    @Override
    protected void additionalTests() {

        System.out.println("Testing STSP templates");
        testStspTemplate("errors/generic_error_page.html");
        testStspTemplate("goasp/base.html");
        testStspTemplate("goasp/login_form.html");
        testStspTemplate("goasp/register_form.html");
        testStspTemplate("goasp/request_form.html");

        System.out.println();
    }
}
