package ru.andreysosnovyy;

import ru.andreysosnovyy.config.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ru.andreysosnovyy.config.tables.*;

public class DBHandler extends DBConfig {

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        String connectionSrt = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(connectionSrt, DB_USER, DB_PASS);
    }


    // добавление пользователя в базу данных
    public void addUserInfo(long ui, String fn, String ln, String un) {
        String request = "INSERT INTO " + UsersInfo.TABLE_NAME + " (" +
                UsersInfo.USER_ID + "," + UsersInfo.FIRST_NAME + "," + UsersInfo.LAST_NAME + "," +
                UsersInfo.USERNAME + ")" + "VALUES(?,?,?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, ui);
            preparedStatement.setString(2, fn);
            preparedStatement.setString(3, ln);
            preparedStatement.setString(4, un);
            preparedStatement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
