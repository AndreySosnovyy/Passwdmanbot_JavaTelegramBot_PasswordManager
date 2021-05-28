package ru.andreysosnovyy;

import ru.andreysosnovyy.config.DBConfig;
import ru.andreysosnovyy.tables.Password;
import ru.andreysosnovyy.tables.RepositoryPassword;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.DBPasswordRecordsBuilder;

import java.sql.*;

public class DBHandler extends DBConfig {

    public Connection getConnection() throws SQLException {
        String connectionSrt = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        return DriverManager.getConnection(connectionSrt, DB_USER, DB_PASS);
    }


    // добавление нового пользователя в базу данных
    public void addNewUser(User user) {
        String request = "INSERT INTO " + User.Table.TABLE_NAME + " (" +
                User.Table.USER_ID + "," + User.Table.FIRST_NAME + "," + User.Table.LAST_NAME + "," +
                User.Table.USERNAME + ")" + "VALUES(?,?,?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // возвращает пользователя с указанным id
    public ResultSet getUser(long userId) {
        ResultSet resultSet = null;

        String request = "SELECT * FROM " + User.Table.TABLE_NAME + " where " +
                User.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }


    // добавить состояние для чата с конкретным пользователем
    public void addNewUserState(User user) {
        String request = "INSERT INTO " + UserState.Table.TABLE_NAME + " (" +
                UserState.Table.USER_ID + "," + UserState.Table.STATE + ")" + "VALUES(?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, UserState.Names.BASE_NO_REPOSITORY_PASSWORD);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // получить состояние чата конкретного пользователя
    public String getUserState(long userId) {
        ResultSet resultSet = null;

        String request = "SELECT " + UserState.Table.STATE + " FROM " + UserState.Table.TABLE_NAME + " WHERE " +
                UserState.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            assert resultSet != null;
            if (resultSet.next()) {
                return resultSet.getString(UserState.Table.STATE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // обновить состояние чата пользователя
    public void setUserState(Long chatId, String newState) {
        String request = "UPDATE users_states SET " + UserState.Table.STATE + "=? WHERE " +
                UserState.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setString(1, newState);
            preparedStatement.setLong(2, chatId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // добавить пароль от репозитория пользователя
    public void addRepositoryPasswordHash(long userId, String hash) {
        String request = "INSERT INTO " + RepositoryPassword.Table.TABLE_NAME + " (" +
                RepositoryPassword.Table.USER_ID + "," +
                RepositoryPassword.Table.REPOSITORY_PASSWORD + ")" + "VALUES(?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, hash);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // получить хэш пароля пользователя
    public String getRepositoryPasswordHash(long userId) {
        ResultSet resultSet = null;

        String request = "SELECT " + RepositoryPassword.Table.REPOSITORY_PASSWORD +
                " FROM " + RepositoryPassword.Table.TABLE_NAME + " WHERE " +
                RepositoryPassword.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            assert resultSet != null;
            if (resultSet.next()) {
                return resultSet.getString(RepositoryPassword.Table.REPOSITORY_PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // добавить новый пароль для пользователя
    public boolean addPasswordRecord(DBPasswordRecordsBuilder.DBPasswordRecord record) {
        String request = "INSERT INTO " + Password.Table.TABLE_NAME + " (" +
                Password.Table.USER_ID + "," + Password.Table.SERVICE_NAME + "," +
                Password.Table.LOGIN + "," + Password.Table.PASSWORD + "," +
                Password.Table.COMMENT + ")" + "VALUES(?,?,?,?,?)";
        try {

            if (record == null) return false;

            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, record.getUserId());
            preparedStatement.setString(2, record.getServiceName());
            preparedStatement.setString(3, record.getLogin());
            preparedStatement.setString(4, record.getPassword());
            preparedStatement.setString(5, record.getComment());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // для админа
    public void clearDB() {
        String request = "DELETE FROM users WHERE `id` > 0";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
