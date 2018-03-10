package ca.uqac.inf135.group3.tp3.handlers;

import ca.uqac.inf135.group3.tp3.model.database.RESTodoDatabase;
import ca.uqac.inf135.group3.tp3.pipeline.RouteHandler;

public abstract class DatabaseHandler implements RouteHandler {
    private final RESTodoDatabase database;

    public DatabaseHandler(RESTodoDatabase database) {
        this.database = database;
    }

    protected RESTodoDatabase getDatabase() {
        return database;
    }
}
