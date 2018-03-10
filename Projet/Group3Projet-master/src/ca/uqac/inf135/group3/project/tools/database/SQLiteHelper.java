package ca.uqac.inf135.group3.project.tools.database;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;

import java.sql.*;

public abstract class SQLiteHelper {
    private static final String JDBC_CONNECT_FORMAT = "jdbc:sqlite:%s";

    private final String filename;
    private JdbcConnectionSource connectionSource;

    private boolean debugMode = false;

    public SQLiteHelper(String filename, boolean debugMode) throws SQLException {
        this.filename = filename;
        this.debugMode = debugMode;

        applyDebugMode();

        //Load SQLite JDBC class
        org.sqlite.JDBC.class.getName();

        //Open connection to sqlite database on specified filename
        JdbcConnectionSource connectionSource;
        try {
            connectionSource = new JdbcConnectionSource(String.format(JDBC_CONNECT_FORMAT, filename));
        } catch (SQLException e) {
            connectionSource = null;
        }
        this.connectionSource = connectionSource;

        if (isConnected()) {
            createTables();
        }
    }
    public SQLiteHelper(String filename) throws SQLException {
        this(filename, false);
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    protected void applyDebugMode() {
        System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, debugMode ? "DEBUG" : "ERROR");
    }

    protected abstract void createTables() throws SQLException;

    public boolean isConnected() {
        return connectionSource != null;
    }

    public String getFilename() {
        return filename;
    }

    protected JdbcConnectionSource getConnectionSource() {
        return connectionSource;
    }

    protected SQLException parseSQLException(SQLException exception) {

        if (exception.getCause() != null) {
            if (exception.getCause() instanceof SQLException) {
                final String sqlMessage = exception.getCause().getMessage();

                String tableField = null;
                if (sqlMessage.startsWith(SQLUniqueConstraintException.UNIQUE_MESSAGE_PREFIX)) {
                    tableField = sqlMessage.substring(SQLUniqueConstraintException.UNIQUE_MESSAGE_PREFIX.length());
                } else if (sqlMessage.startsWith(SQLUniqueConstraintException.PK_MESSAGE_PREFIX)) {
                    tableField = sqlMessage.substring(SQLUniqueConstraintException.PK_MESSAGE_PREFIX.length());
                }

                if (tableField != null) {
                    //Remove closing ")"
                    tableField = tableField.substring(0, tableField.length() - 1);

                    //Split table.field
                    final int dotPos = tableField.indexOf('.');
                    final String table;
                    final String field;
                    if (dotPos >= 0) {
                        table = tableField.substring(0, dotPos);
                        field = tableField.substring(dotPos + 1);
                    } else {
                        table = "";
                        field = tableField;
                    }

                    return new SQLUniqueConstraintException(table, field, "Unique constraint validation failed", exception);
                }
            }
        }

        return exception;
    }

}
