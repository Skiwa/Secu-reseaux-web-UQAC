package ca.uqac.inf135.group3.tp3;

import ca.uqac.inf135.group3.tp3.filters.JSONRequestBodyFilter;
import ca.uqac.inf135.group3.tp3.filters.JWTAuthenticationRequiredFilter;
import ca.uqac.inf135.group3.tp3.handlers.*;
import ca.uqac.inf135.group3.tp3.handlers.api.*;
import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.pipeline.RouteFilter;
import ca.uqac.inf135.group3.tp3.pipeline.RouteServer;
import ca.uqac.inf135.group3.tp3.tools.HttpMethod;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

public class RESTodo {
    private static final int DEFAULT_PORT = 8080;

    private static int getPortFromArgument(String arg) throws Exception {
        try {
            return Integer.parseInt(arg, 10);
        } catch (Exception e) {
            throw new Exception(String.format("Invalid port parameter: %s", arg));
        }
    }

    private static void runTestMode(String[] args) {
        int exitCode = 0;

        //Test mode
        System.out.println("Executing RESTodo test mode");
        System.out.println();

        //Make sure we are able to load or create database
        System.out.println("Testing database...");
        try {
            final RESTodoDatabase database = new RESTodoDatabase();
            System.out.println("Database OK");

            System.out.println("Listing existing users and todos:");
            List<User> userList = database.selectAllUsers();
            for (User user : userList) {
                System.out.println("  User: " + user.getJSON().toString());

                List<Todo> todoList = database.selectAllUserTodos(user);
                for (Todo todo : todoList) {
                    System.out.println("    Todo: " + todo.getJSON().toString());
                }
            }
            System.out.println();
        } catch (SQLException e) {
            exitCode = 1;
            System.out.println("*** An error occurred testing database ***");
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("Testing port number...");
        //Testing port number
        int port = -1;
        if (args.length >= 2) {
            try {
                port = getPortFromArgument(args[1]);
                System.out.println(String.format("Using port number %d", port));

                if (port < 0 || port > 65335) {
                    exitCode = 2;
                    System.out.println("*** Port must be in range [0; 65535] ***");
                    port = -1;
                }
            } catch (Exception e) {
                exitCode = 3;
                port = -1;
                System.out.println(String.format("*** %s ***", e.getMessage()));
            }
        } else {
            port = DEFAULT_PORT;
            System.out.println(String.format("No port specified, using default port: %d", port));
        }

        System.out.println();
        if (port >= 0) {
            System.out.println("Testing REST API server...");
            RouteServer server;
            try {
                server = new RouteServer(port);
            } catch (Exception e) {
                exitCode = 4;
                server = null;
                System.out.println(String.format("*** Error creating server: %s ***", e.getMessage()));
                e.printStackTrace();
            }

            if (server != null) {
                if (server.start()) {
                    System.out.println("Server started successfully");

                    if (server.stop()) {
                        System.out.println("Server stopped successfully");
                    } else {
                        exitCode = 6;
                        System.out.println("*** Server could not be stopped ***");
                    }
                } else {
                    exitCode = 5;
                    System.out.println("*** Server could not be started ***");
                }
            }
        } else {
            System.out.println("Port is invalid, skipping server socket test.");
        }


        System.out.println();
        System.out.println("End of test mode. Exiting.");

        System.exit(exitCode); //In case the server was started but not stopped
    }

    public static void main (String[] args) {

        if (args.length >= 1 && "test".equals(args[0])) {
            runTestMode(args);
            return;
        }

        final int port;
        if (args.length >= 1) {
            try {
                port = getPortFromArgument(args[0]);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return;
            }
        } else {
            port = DEFAULT_PORT;
        }

        //Create / load database
        System.out.println("Creating or loading database...");
        final RESTodoDatabase database;
        try {
            database = new RESTodoDatabase();
        } catch (SQLException e) {
            System.err.println("Unable to create or load database.");
            e.printStackTrace();
            return;
        }
        System.out.println("Database done");
        System.out.println();

        //Create server
        System.out.println("Preparing server...");
        final RouteServer server;
        try {
            server = new RouteServer(port);
        } catch (Exception e) {
            System.err.println(String.format("Error preparing server: %s", e.getMessage()));
            e.printStackTrace();
            return;
        }

        //Prepare common filter data
        JSONObject jsonTodoFilterTemplate = new JSONObject()
                .add("content", "Todo content (the thing to do)")
                .add("done", "true or false, whether the thing to do is done or not");
        RouteFilter jsonTodoFilter = new JSONRequestBodyFilter(jsonTodoFilterTemplate);
        JSONObject jsonLoginFilterTemplate = new JSONObject()
                .add("username", "Either username or email address of the new user")
                .add("password", "Password used at registration");
        RouteFilter authenticationRequiredFilter = new JWTAuthenticationRequiredFilter(database);

        System.out.println("Registering API routes...");

        //Declare user related API
        server.addRoute(HttpMethod.GET, "/api/users", new ListUsersHandler(database))
                .addPreFilter(authenticationRequiredFilter);
        server.addRoute(HttpMethod.GET, "/api/users/", new GetUserHandler(database));
        server.addRoute(HttpMethod.POST, "/api/users", new RegisterUserHandler(database, jsonLoginFilterTemplate))
                .addPreFilter(new JSONRequestBodyFilter(new JSONObject()
                                .add("username", "Username for the new user (must not already exist)")
                                .add("password", "Password the user will use to log in")
                                .add("mail", "Email address of the user (must not already exist)")
                        )
                );

        //Declare login related API
        server.addRoute(HttpMethod.POST, "/api/login", new LoginHandler(database))
                .addPreFilter(new JSONRequestBodyFilter(jsonLoginFilterTemplate));

        //Declare TO-DO related API
        server.addRoute(HttpMethod.GET, "/api/todos", new ListTodosHandler(database))
                .addPreFilter(authenticationRequiredFilter);
        server.addRoute(HttpMethod.POST, "/api/todos", new CreateTODOHandler(database))
                .addPreFilter(authenticationRequiredFilter)
                .addPreFilter(jsonTodoFilter);
        server.addRoute(HttpMethod.DELETE, "/api/todos/", new DeleteTodoHandler(database))
                .addPreFilter(authenticationRequiredFilter);
        server.addRoute(HttpMethod.PATCH, "/api/todos/", new UpdateTodoHandler(database))
                .addPreFilter(authenticationRequiredFilter)
                .addPreFilter(jsonTodoFilter);
        //NOTE: the GetTodo route is not required, but here it is anyway
        server.addRoute(HttpMethod.GET, "/api/todos/", new GetTodoHandler(database))
                .addPreFilter(authenticationRequiredFilter);

        //All other routes are not found
        server.addRoute(null, "/", new NotFoundHandler());

        System.out.println();
        System.out.println("Server prepared");

        System.out.println();
        if (server.start()) {
            System.out.println(String.format("Server started on port %d.", port));
        }
        else {
            System.err.println("Server could not be started");
        }
    }
}
