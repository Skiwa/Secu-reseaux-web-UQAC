package ca.uqac.inf135.group3.project;

import ca.uqac.inf135.group3.project.handlers.NotFoundHandler;
import ca.uqac.inf135.group3.project.pipeline.RouteServer;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;

import java.sql.SQLException;

public abstract class WebApp {
    final String[] args;

    private final String appDescription;

    protected WebApp(String[] args, String description) {
        this.args = args;
        this.appDescription = description;
    }

    private int getPortFromArgument(int index) throws Exception {
        try {
            return Integer.parseInt(args[index], 10);
        } catch (Exception e) {
            throw new Exception(String.format("Invalid port parameter: %s", args[index]));
        }
    }

    protected abstract int getDefaultPort();

    protected abstract SQLiteHelper getDatabase() throws SQLException;

    protected abstract void doPostLoadDatabaseTasks();

    protected abstract void registerCustomRoutes(RouteServer server);

    protected abstract void additionalTests();

    private boolean createOrLoadDatabase() {
        System.out.println(String.format("Creating or loading %s database...", appDescription));
        final SQLiteHelper database;
        try {
            database = getDatabase();
        } catch (SQLException e) {
            System.err.println(String.format("Unable to create or load %s database.", appDescription));
            e.printStackTrace();
            return false;
        }
        System.out.println(String.format(
                "%s database loaded: %s",
                appDescription,
                database.getFilename())
        );
        System.out.println();

        doPostLoadDatabaseTasks();
        return true;
    }

    private RouteServer prepareRouteServer(int port, boolean registerRoutes) {
        System.out.println(String.format("Preparing %s route server...", appDescription));
        final RouteServer server;
        try {
            server = new RouteServer(port);
        } catch (Exception e) {
            System.err.println(String.format("Error preparing server: %s", e.getMessage()));
            e.printStackTrace();
            return null;
        }

        if (registerRoutes) {
            System.out.println();
            System.out.println(String.format("Registering %s routes...", appDescription));
            registerCustomRoutes(server);

            //All other routes are not found
            server.addRoute(null, "/", new NotFoundHandler());
        }

        System.out.println();
        System.out.println("Server prepared");

        System.out.println();
        if (server.start()) {
            System.out.println(String.format("Server started on port %d.", port));
        }
        else {
            System.err.println("Server could not be started");
        }


        return server;
    }

    protected void runMode() {

        //Find out port to use
        final int port;
        if (args.length >= 1) {
            try {
                port = getPortFromArgument(0);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return;
            }
        } else {
            port = getDefaultPort();
        }

        createOrLoadDatabase();

        RouteServer server = prepareRouteServer(port, true);
    }

    protected void testMode() {
        int exitCode = 0;

        //Test mode
        System.out.println(String.format("Executing %s in test mode", appDescription));
        System.out.println();

        //Make sure we are able to load or create database
        System.out.println(String.format("Testing %s database...", appDescription));
        if (createOrLoadDatabase()) {
            System.out.println("Database OK");
            System.out.println();
        }
        else {
            exitCode = 1;
            System.out.println(String.format("*** An error occurred testing %s database ***", appDescription));
        }

        System.out.println();
        System.out.println("Testing port number...");
        //Testing port number
        int port = -1;
        if (args.length >= 2) {
            try {
                port = getPortFromArgument(1);
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
            port = getDefaultPort();
            System.out.println(String.format("No port specified, using default port: %d", port));
        }

        System.out.println();
        if (port >= 0) {
            System.out.println(String.format("Testing %s route server...", appDescription));
            RouteServer server = prepareRouteServer(port, false);
            if (server == null) {
                exitCode = 4;
                server = null;
                System.out.println(String.format("*** Error creating %s route server on port %d ***", appDescription, port));
            }
            else {
                if (server.isValid()) {
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
        System.out.println(String.format("Performing additional %s specific tests...", appDescription));
        System.out.println();
        additionalTests();

        System.out.println();
        System.out.println("End of test mode. Exiting.");

        System.exit(exitCode); //In case the server was started but not stopped
    }

    public void start() {
        if (args.length >= 1 && "test".equals(args[0].toLowerCase())) {
            testMode();
        }
        else {
            runMode();
        }
    }
}
