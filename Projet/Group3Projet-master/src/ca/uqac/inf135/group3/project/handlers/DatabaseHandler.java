package ca.uqac.inf135.group3.project.handlers;

import ca.uqac.inf135.group3.project.pipeline.ExchangeHelper;
import ca.uqac.inf135.group3.project.pipeline.RouteHandler;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;

import java.io.IOException;

public abstract class DatabaseHandler implements RouteHandler {
    private final SQLiteHelper database;
    private ExchangeHelper exchangeHelper;

    public DatabaseHandler(SQLiteHelper database) {
        this.database = database;
    }

    protected SQLiteHelper getDatabase() {
        return database;
    }

    @Override
    public void handle(ExchangeHelper exchangeHelper) throws IOException {
        this.exchangeHelper = exchangeHelper;
        handle();
    }

    protected ExchangeHelper getExchangeHelper() {
        return exchangeHelper;
    }

    protected abstract void handle() throws IOException;
}
