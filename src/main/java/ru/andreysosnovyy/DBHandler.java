package ru.andreysosnovyy;

import ru.andreysosnovyy.config.DBConfig;

import java.sql.*;

import ru.andreysosnovyy.tables.*;

public class DBHandler extends DBConfig {

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        String connectionSrt = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Class.forName("com.mysql.jdbc.Driver");
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
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getUser(long userId) {
        ResultSet resultSet = null;

        String request = "SELECT * FROM " + User.Table.TABLE_NAME + " where " +
                User.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return resultSet;
    }
}
