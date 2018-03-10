package ca.uqac.inf135.group3.project.model.database;

import ca.uqac.inf135.group3.project.model.entities.restodo.Todo;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RESTodoDatabase extends SQLiteHelper {
    private static final String FILE_NAME = "restodo.sqlite";

    public RESTodoDatabase(boolean debug) throws SQLException {
        super(FILE_NAME, debug);
    }

    @Override
    protected void createTables() throws SQLException {
        TableUtils.createTableIfNotExists(getConnectionSource(), Todo.class);
    }

    /********************************************************
     * User related methods
     ********************************************************/

    private Dao<Todo, Integer> getTodoDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), Todo.class);
    }

    public Todo getTodoByID(int todoID) throws SQLException {
        return getTodoDao().queryForId(todoID);
    }

    public List<Todo> getAllUserTodos(String userID) throws SQLException {
        return getTodoDao().queryForEq("userID", userID);
    }

    public void createTodo(Todo todo) throws SQLException {
        try {
            getTodoDao().create(todo);
        }
        catch (SQLException e) {
            throw parseSQLException(e);
        }
    }

    public void updateTodo(Todo todo) throws SQLException {
        getTodoDao().update(todo);
    }

    public void deleteTodo(Todo todo) throws SQLException {
        getTodoDao().delete(todo);
    }
}
