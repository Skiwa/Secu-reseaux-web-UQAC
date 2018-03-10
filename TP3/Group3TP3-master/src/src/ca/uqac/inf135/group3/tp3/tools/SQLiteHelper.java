package ca.uqac.inf135.group3.tp3.tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SQLiteHelper {
    private static final String JDBC_SQLITE_DRIVER_CLASS_NAME = "org.sqlite.JDBC";
    private static final String JDBC_CONNECT_FORMAT = "jdbc:sqlite:%s";

    public static final String TYPE_INT = "INTEGER";
    public static final String TYPE_TEXT = "TEXT";

    private final String filename;
    private Connection connection;
    private final Statement statement;
    private final Map<String, PreparedStatement> preparedStatements = new HashMap<>();

    public SQLiteHelper(String filename) throws SQLException {
        this.filename = filename;

        //Load SQLite JDBC class
        org.sqlite.JDBC.class.getName();

        //Open connection to sqlite datbase on specified filename
        Connection connection;
        try {
            connection = DriverManager.getConnection(String.format(JDBC_CONNECT_FORMAT, filename));
        } catch (SQLException e) {
            connection = null;
        }
        this.connection = connection;

        if (this.connection != null) {
            Statement statement;
            try {
                statement = this.connection.createStatement();
            } catch (SQLException e) {
                statement = null;
            }
            this.statement = statement;
        }
        else {
            this.statement = null;
        }

        if (isConnected()) {
            createTables();
        }
    }

    protected int executeNoResult(String sql) throws SQLException {
        try {
            return statement.executeUpdate(sql); //Returns number of affected rows
        }
        catch (SQLException e) {
            throw new SQLException("Error executing sql query", new Exception(sql ,e));
        }
    }

    protected int executePrepNoResult(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeUpdate();
    }

    protected ResultSet executePrepQuery(PreparedStatement preparedStatement) throws SQLException {
        return preparedStatement.executeQuery();
    }

    protected PreparedStatement getPreparedStatement(String name, String query) throws SQLException {
        if (isConnected()) {
            //If prepared statement already exist, reuse it, otherwise create it
            if (preparedStatements.containsKey(name)) {
                return preparedStatements.get(name);
            }
            else {
                final PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatements.put(name, preparedStatement);
                return preparedStatement;
            }
        }
        else {
            return null;
        }
    }
    protected PreparedStatement getPreparedStatementReturningID(String name, String query) throws SQLException {
        if (isConnected()) {
            //If prepared statement already exist, reuse it, otherwise create it
            if (preparedStatements.containsKey(name)) {
                return preparedStatements.get(name);
            }
            else {
                final PreparedStatement preparedStatement = connection.prepareStatement(query, new String[] {"id"});
                preparedStatements.put(name, preparedStatement);
                return preparedStatement;
            }
        }
        else {
            return null;
        }
    }

    protected abstract void createTables() throws SQLException;

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public class Table {
        private final String tableName;
        private final StringBuilder createTableBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        private boolean firstInstruction = true;

        public Table(String tableName) {
            this.tableName = tableName.toUpperCase();
            append(getTableName());
            append(" (");
        }

        public String getTableName() {
            return tableName;
        }

        private void append(String append) {
            createTableBuilder.append(append);
        }

        private void handleFirstInstruction() {
            //Add a field separator if necessary
            if (firstInstruction) {
                firstInstruction = false;
            }
            else {
                append(",");
            }
        }

        private Table addColumn(String colName, String colType, boolean notNull, boolean unique, boolean pk) {
            handleFirstInstruction();

            //Column name is mandatory
            append(colName.toUpperCase());

            //Column type is mandatory
            append(" ");
            append(colType.toUpperCase());

            //Add "PRIMARY KEY" or "UNIQUE" if necessary
            if (pk) {
                append(" PRIMARY KEY");
            }
            else if (unique) {
                append(" UNIQUE");
            }

            //Add NULL or NOT NULL
            if (notNull) {
                append(" NOT");
            }
            append(" NULL");

            return this;
        }
        public Table addColumn(String colName, String colType, boolean notNull, boolean unique) {
            return addColumn(colName, colType, notNull, unique, false);
        }
        public Table addColumn(String colName, String colType, boolean notNull) {
            return addColumn(colName, colType, notNull, false);
        }
        public Table addColumn(String colName, String colType) {
            return addColumn(colName, colType, false);
        }
        public Table addColumn(String colName) {
            return addColumn(colName, TYPE_TEXT);
        }

        public Table addSinglePK(String colName, String colType) {
            addColumn(colName, colType, true, false, true);

            return this;
        }
        public Table addSinglePK(String colName) {
            return addSinglePK(colName, TYPE_INT);
        }

        public Table addFK(String cols[], String refTable, String[] refCols) {
            handleFirstInstruction();

            final int colCount = cols.length;
            append("FOREIGN KEY (");
            for (int i = 0; i < colCount; ++i) {
                append(cols[i].toUpperCase());

                if (i != 0) {
                    append(",");
                }
            }

            append(") REFERENCES ");
            append(refTable);
            append(" (");

            for (int i = 0; i < colCount; ++i) {
                append(refCols[i].toUpperCase());

                if (i != 0) {
                    append(",");
                }
            }
            append(")");

            return this;
        }
        public Table addFK(String cols[], Table refTable, String[] refCols) {
            return addFK(cols, refTable.getTableName(), refCols);
        }
        public Table addFK(String col, String refTable, String refCol) {
            return addFK(new String[] {col}, refTable, new String[] { refCol});
        }
        public Table addFK(String col, Table refTable, String refCol) {
            return addFK(col, refTable.getTableName(), refCol);
        }

        public Table addMultiPk(String[] cols) {
            handleFirstInstruction();

            final int colCount = cols.length;
            append("PRIMARY KEY (");
            for (int i = 0; i < colCount; ++i) {
                append(cols[i].toUpperCase());

                if (i != 0) {
                    append(",");
                }
            }
            append(")");

            return this;
        }

        public void create() throws SQLException {
            SQLiteHelper.this.executeNoResult(this.toString());
        }

        @Override
        public String toString() {
            return createTableBuilder.toString() + ");";
        }
    }
}
