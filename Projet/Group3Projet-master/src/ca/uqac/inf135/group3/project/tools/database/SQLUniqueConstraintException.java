package ca.uqac.inf135.group3.project.tools.database;

import java.sql.SQLException;

public class SQLUniqueConstraintException extends SQLException {
    public static final String PK_MESSAGE_PREFIX = "[SQLITE_CONSTRAINT_PRIMARYKEY]  A PRIMARY KEY constraint failed (UNIQUE constraint failed: ";
    public static final String UNIQUE_MESSAGE_PREFIX = "[SQLITE_CONSTRAINT_UNIQUE]  A UNIQUE constraint failed (UNIQUE constraint failed: ";

    private final String tableName;
    private final String fieldName;

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason, String SQLState) {
        super(reason, SQLState);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason) {
        super(reason);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName) {
        super();
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, Throwable cause) {
        super(cause);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason, Throwable cause) {
        super(reason, cause);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason, String sqlState, Throwable cause) {
        super(reason, sqlState, cause);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public SQLUniqueConstraintException(String tableName, String fieldName, String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
