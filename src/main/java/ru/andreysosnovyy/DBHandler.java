package ru.andreysosnovyy;

import ru.andreysosnovyy.config.DBConfig;

import java.sql.*;

import ru.andreysosnovyy.tables.*;

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
                UserState.Table.USER_ID + "," +  UserState.Table.STATE + ")" + "VALUES(?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, "base");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // получить состояние чата конкретного пользователя
    public ResultSet getUserState(long userId) {
        ResultSet resultSet = null;

        String request = "SELECT * FROM " + UserState.Table.TABLE_NAME + " where " +
                UserState.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }
}
