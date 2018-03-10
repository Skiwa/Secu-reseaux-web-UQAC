package ca.uqac.inf135.group3.project;

import ca.uqac.inf135.group3.project.filters.GoaspAccessTokenRequiredFilter;
import ca.uqac.inf135.group3.project.filters.JSONRequestBodyFilter;
import ca.uqac.inf135.group3.project.handlers.restodo.*;
import ca.uqac.inf135.group3.project.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.project.pipeline.RouteServer;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;
import ca.uqac.inf135.group3.project.tools.http.HttpMethod;
import ca.uqac.inf135.group3.project.tools.json.JSONObject;

import java.sql.SQLException;

public class RESTodoApp extends WebApp {
    private static final int DEFAULT_PORT = 8080;
    private static final String APP_NAME = "RESTodo";

    private RESTodoDatabase database;

    public static void main (String[] args) {
        new RESTodoApp(args)
                .start();
    }

    protected RESTodoApp(String[] args) {
        super(args, APP_NAME);
    }

    @Override
    protected int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    protected SQLiteHelper getDatabase() throws SQLException {
        if (database == null) {
            database = new RESTodoDatabase(false);
        }
        return database;
    }

    @Override
    protected void doPostLoadDatabaseTasks() {
        //Nothing to do for this app
    }

    @Override
    protected void registerCustomRoutes(RouteServer server) {
        //Generic templates and filters
        JSONRequestBodyFilter todoRequestBodyFilter = new JSONRequestBodyFilter(new JSONObject()
                .add("content", "String. Content of the todo")
                .add("done", "Boolean. True indicates that the item to do is done. False if yet to do.")
        );

        server.addRoute(HttpMethod.GET, "/api/todos", new ListTodosHandler(database))
                .addPreFilter(new GoaspAccessTokenRequiredFilter("restodo_read"))
        ;
        server.addRoute(HttpMethod.POST, "/api/todos", new CreateTODOHandler(database))
                .addPreFilter(new GoaspAccessTokenRequiredFilter("restodo_add"))
                .addPreFilter(todoRequestBodyFilter)
        ;
        server.addRoute(HttpMethod.GET, "/api/todos/", new GetTodoHandler(database))
                .addPreFilter(new GoaspAccessTokenRequiredFilter("restodo_read"))
        ;
        server.addRoute(HttpMethod.PATCH, "/api/todos/", new UpdateTodoHandler(database))
                .addPreFilter(new GoaspAccessTokenRequiredFilter("restodo_edit"))
                .addPreFilter(todoRequestBodyFilter)
        ;
        server.addRoute(HttpMethod.DELETE, "/api/todos/", new DeleteTodoHandler(database))
                .addPreFilter(new GoaspAccessTokenRequiredFilter("restodo_del"))
        ;
    }

    @Override
    protected void additionalTests() {
        //No additional test to perform in RESTodo
    }
}
