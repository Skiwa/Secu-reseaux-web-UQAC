package ca.uqac.inf135.group3.project.model.database;

import ca.uqac.inf135.group3.project.model.entities.goasp.*;
import ca.uqac.inf135.group3.project.tools.database.SQLUniqueConstraintException;
import ca.uqac.inf135.group3.project.tools.database.SQLiteHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class GoaspDatabase extends SQLiteHelper {
    private static final String FILE_NAME = "goasp.sqlite";

    public GoaspDatabase(boolean debug) throws SQLException {
        super(FILE_NAME, debug);
    }

    @Override
    protected void createTables() throws SQLException {
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspSinglePageApp.class);
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspScope.class);
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspUser.class);
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspSession.class);
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspAuthCode.class);
        TableUtils.createTableIfNotExists(getConnectionSource(), GoaspToken.class);
    }


    /********************************************************
     * Single Page Appliations (SPA) related methods
     ********************************************************/

    private Dao<GoaspSinglePageApp, String> getSpaDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspSinglePageApp.class);
    }

    public void createSpa(GoaspSinglePageApp spa) throws SQLException {
        for(;;) {
            try {
                getSpaDao().create(spa);
                return;
            } catch (SQLException e) {
                SQLException parsedException = parseSQLException(e);

                if (parsedException instanceof SQLUniqueConstraintException) {
                    spa.generateID();
                }
                else {
                    throw parsedException;
                }
            }
        }
    }

    public GoaspSinglePageApp getSpaByID(String clientID) throws SQLException {
        return getSpaDao().queryForId(clientID);
    }

    public GoaspSinglePageApp getSpaByName(String name) throws SQLException {
        List<GoaspSinglePageApp> spas = getSpaDao().query(
                getSpaDao().queryBuilder()
                        .where()
                        .raw("lower(name) = lower(?)", new SelectArg(SqlType.STRING, name))
                        .prepare()
        );
        if (spas.size() == 1) {
            return spas.get(0);
        }

        return null;
    }


    /********************************************************
     * Scope related methods
     ********************************************************/

    private Dao<GoaspScope, String> getScopeDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspScope.class);
    }

    private void expandScope(GoaspScope scope) throws SQLException {
        if (scope != null) {
            scope.setApp(getSpaByID(scope.getApp().getID()));
        }
    }

    public void createScope (GoaspScope scope) throws SQLException {
        try {
            getScopeDao().create(scope);
        }
        catch (SQLException e) {
            throw parseSQLException(e);
        }
    }

    public GoaspScope getScopeByName(String name) throws SQLException {
        if (name != null) {
            GoaspScope scope = getScopeDao().queryForId(name.toLowerCase());
            expandScope(scope);
            return scope;
        }
        return null;
    }


    /********************************************************
     * User related methods
     ********************************************************/

    private Dao<GoaspUser, Integer> getUserDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspUser.class);
    }

    public void createUser (GoaspUser user) throws SQLException {
        try {
            getUserDao().create(user);
        }
        catch (SQLException e) {
            throw parseSQLException(e);
        }
    }

    public GoaspUser getUserByID(int id) throws SQLException {
        return getUserDao().queryForId(id);
    }

    public GoaspUser getUserByUsername(String username) throws SQLException {
        List<GoaspUser> users = getUserDao().query(
                getUserDao().queryBuilder()
                        .where()
                        .raw("lower(username) = lower(?)", new SelectArg(SqlType.STRING, username))
                        .prepare()
        );
        if (users.size() == 1) {
            return users.get(0);
        }

        return null;
    }

    public GoaspUser getUserByEmail(String email) throws SQLException {
        List<GoaspUser> users = getUserDao().query(
                getUserDao().queryBuilder()
                        .where()
                        .raw("lower(email) = lower(?)", new SelectArg(SqlType.STRING, email))
                        .prepare()
        );
        if (users.size() == 1) {
            return users.get(0);
        }

        return null;
    }


    /********************************************************
     * Session related methods
     ********************************************************/

    private Dao<GoaspSession, String> getSessionDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspSession.class);
    }

    private void expandSession(GoaspSession session) throws SQLException {
        if (session != null) {
            session.setUser(getUserByID(session.getUser().getId()));
        }
    }

    private void handleSessionException(SQLException e, GoaspSession session) throws SQLException {
        SQLException parsedException = parseSQLException(e);

        if (parsedException instanceof SQLUniqueConstraintException) {
            if ("securityToken".equals(((SQLUniqueConstraintException) parsedException).getFieldName())) {
                session.generateSecurityToken();
            }
            else {
                session.generateID();
            }
        }
        else {
            throw parsedException;
        }
    }

    public void createSession(GoaspSession session) throws SQLException {
        for(;;) {
            try {
                getSessionDao().create(session);
                return;
            } catch (SQLException e) {
                handleSessionException(e, session);
            }
        }
    }

    public GoaspSession getSessionByID(String id) throws SQLException {
        final GoaspSession session = getSessionDao().queryForId(id);

        expandSession(session);

        return session;
    }

    public void updateSession(GoaspSession session) throws SQLException {
        for(;;) {
            try {
                getSessionDao().update(session);
                return;
            } catch (SQLException e) {
                handleSessionException(e, session);
            }
        }
    }

    public void deleteSession(GoaspSession session) throws SQLException {
        getSessionDao().delete(session);
    }


    /********************************************************
     * AuthorizationCode (Auth) related methods
     ********************************************************/

    private Dao<GoaspAuthCode, String> getAuthDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspAuthCode.class);
    }

    private void expandAuth(GoaspAuthCode auth) throws SQLException {
        if (auth != null) {
            auth.setApp(getSpaByID(auth.getApp().getID()));
            auth.setUser(getUserByID(auth.getUser().getId()));
        }
    }

    public void createAuth(GoaspAuthCode auth) throws SQLException {
        for(;;) {
            try {
                getAuthDao().create(auth);
                return;
            } catch (SQLException e) {
                SQLException parsedException = parseSQLException(e);

                if (parsedException instanceof SQLUniqueConstraintException) {
                    auth.generateCode();
                }
                else {
                    throw parsedException;
                }
            }
        }
    }

    public GoaspAuthCode getAuthByCode(String code) throws SQLException {
        final GoaspAuthCode auth = getAuthDao().queryForId(code);

        expandAuth(auth);

        return auth;
    }

    public void deleteAuth(GoaspAuthCode authCode) throws SQLException {
        getAuthDao().delete(authCode);
    }


    /********************************************************
     * Token related methods
     ********************************************************/

    private Dao<GoaspToken, String> getTokenDao() throws SQLException {
        applyDebugMode();
        return DaoManager.createDao(getConnectionSource(), GoaspToken.class);
    }

    private void expandToken(GoaspToken token) throws SQLException {
        if (token != null) {
            token.setApp(getSpaByID(token.getApp().getID()));
            token.setUser(getUserByID(token.getUser().getId()));
        }
    }

    public void createToken(GoaspToken token) throws SQLException {
        try {
            getTokenDao().create(token);
        } catch (SQLException e) {
            throw parseSQLException(e);
        }
    }

    public GoaspToken getToken(String token) throws SQLException {
        final GoaspToken goaspToken = getTokenDao().queryForId(token);

        expandToken(goaspToken);

        return goaspToken;
    }

}
