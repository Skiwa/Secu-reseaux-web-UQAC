package ca.uqac.inf135.group3.tp3.model.database;

import ca.uqac.inf135.group3.tp3.model.entities.Todo;
import ca.uqac.inf135.group3.tp3.model.entities.User;
import ca.uqac.inf135.group3.tp3.tools.SQLiteHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RESTodoDatabase extends SQLiteHelper {
    private static final String FILE_NAME = "restodo.sqlite";

    public RESTodoDatabase() throws SQLException {
        super(FILE_NAME);
    }

    @Override
    protected void createTables() throws SQLException {
        new Table("USERS")
                .addSinglePK("id")
                .addColumn("username", TYPE_TEXT, true, true)
                .addColumn("hash",TYPE_TEXT, true)
                .addColumn("salt", TYPE_TEXT, true)
                .addColumn("email", TYPE_TEXT, true, true)
                .create();

        new Table("TODOS")
                .addSinglePK("id")
                .addColumn("user_id", TYPE_TEXT, true)
                .addColumn("content", TYPE_TEXT, true)
                .addColumn("done", TYPE_INT, true)
                .addFK("user_id", "USERS", "id")
                .create();
    }

    private User getUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String hash = rs.getString("hash");
        String salt = rs.getString("salt");
        String email = rs.getString("email");

        return new User(id, username, hash, salt, email);
    }

    public User selectUserByID(int searchID) throws SQLException {
        final String name = "selectUserByID";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, username, hash, salt, email from users where id = ?"
        );

        ps.setInt(1, searchID);

        ResultSet rs = executePrepQuery(ps);

        if (rs.next()) {
            //OK we found a result
            return getUserFromResultSet(rs);
        }
        else {
            return null;
        }
    }
    public User selectUserByUsername(String searchUsername) throws SQLException {
        final String name = "selectUserByUsername";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, username, hash, salt, email from users where upper(username) = upper(?)"
        );

        ps.setString(1, searchUsername);

        ResultSet rs = executePrepQuery(ps);

        if (rs.next()) {
            //OK we found a result
            return getUserFromResultSet(rs);
        }
        else {
            return null;
        }
    }

    public User selectUserByEmail(String searchEmail) throws SQLException {
        final String name = "selectUserByEmail";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, username, hash, salt, email from users where upper(email) = upper(?)"
        );

        ps.setString(1, searchEmail);

        ResultSet rs = executePrepQuery(ps);

        if (rs.next()) {
            //OK we found a result
            return getUserFromResultSet(rs);
        }
        else {
            return null;
        }
    }

    public List<User> selectAllUsers() throws SQLException {
        final List<User> allUsers = new ArrayList<>();

        final String name = "selectAllUsers";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, username, hash, salt, email from users order by upper(username)"
        );

        ResultSet rs = executePrepQuery(ps);

        while (rs.next()) {
            allUsers.add(getUserFromResultSet(rs));
        }

        return allUsers;
    }

    public void insertUser(User user) throws SQLException {
        final String name = "insertUser";
        final PreparedStatement ps = getPreparedStatementReturningID(
                name,
                "insert into users (username, hash, salt, email) values (?, ?, ?, ?)"
        );

        ps.setString(1, user.getUsername());
        ps.setString(2, user.getBase64HashedPassword());
        ps.setString(3, user.getBase64Salt());
        ps.setString(4, user.getEmail());

        final int count = executePrepNoResult(ps);

        ResultSet rs = ps.getGeneratedKeys();

        if (rs != null && rs.next()) {
            int id = rs.getInt(1);
            user.setId(id);
        }
        else {
            throw new SQLException("Insert User should have generated a key.");
        }

        if (count != 1) {
            throw new SQLException(String.format("Insert User should have created 1 row. It created %d", count));
        }
    }

    private Todo getTodoFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int user_id = rs.getInt("user_id");
        String content = rs.getString("content");
        boolean done = rs.getBoolean("done");

        return new Todo(id, user_id, content, done);
    }

    public Todo selectTodoByID(int searchID) throws SQLException {
        final String name = "selectTodoByID";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, user_id, content, done from todos where id = ?"
        );

        ps.setInt(1, searchID);

        ResultSet rs = executePrepQuery(ps);

        if (rs.next()) {
            //OK we found a result
            return getTodoFromResultSet(rs);
        }
        else {
            return null;
        }
    }

    public List<Todo> selectAllUserTodos(User user) throws SQLException {
        final List<Todo> allTodos = new ArrayList<>();

        final String name = "selectAllUserTodos";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "select id, user_id, content, done from todos where user_id = ?"
        );

        ps.setInt(1, user.getId());

        ResultSet rs = executePrepQuery(ps);

        while (rs.next()) {
            allTodos.add(getTodoFromResultSet(rs));
        }

        return allTodos;
    }

    public void insertTodo(Todo todo) throws SQLException {
        final String name = "insertTodo";
        final PreparedStatement ps = getPreparedStatementReturningID(
                name,
                "insert into todos (user_id, content, done) values (?, ?, ?)"
        );

        ps.setInt(1, todo.getUserID());
        ps.setString(2, todo.getContent());
        ps.setBoolean(3, todo.isDone());

        int count = executePrepNoResult(ps);

        ResultSet rs = ps.getGeneratedKeys();

        if (rs != null && rs.next()) {
            int id = rs.getInt(1);
            todo.setId(id);
        }
        else {
            throw new SQLException("Insert Todo should have generated a key.");
        }

        if (count != 1) {
            throw new SQLException(String.format("Insert Todo should have created 1 row. It created %d", count));
        }
    }

    public void updateTodo(Todo todo) throws SQLException {
        final String name = "updateTodo";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "update todos set content = ?, done = ? where id = ?"
        );

        ps.setString(1, todo.getContent());
        ps.setBoolean(2, todo.isDone());
        ps.setInt(3, todo.getId());

        final int count = executePrepNoResult(ps);

        if (count != 1) {
            throw new SQLException(String.format("Update Todo should have updated 1 row. It updated %d", count));
        }

    }

    public void deleteTodo(Todo todo) throws SQLException {
        final String name = "deleteTodo";
        final PreparedStatement ps = getPreparedStatement(
                name,
                "delete from todos where id = ?"
        );

        ps.setInt(1, todo.getId());

        final int count = executePrepNoResult(ps);

        if (count != 1) {
            throw new SQLException(String.format("Delete Todo should have deleted 1 row. It deleted %d", count));
        }

    }
}
