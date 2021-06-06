package ru.andreysosnovyy;

import ru.andreysosnovyy.config.DBConfig;
import ru.andreysosnovyy.tables.PasswordRecord;
import ru.andreysosnovyy.tables.RepositoryPassword;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.Cryption;
import ru.andreysosnovyy.utils.DBPasswordRecordsBuilder;

import javax.crypto.SecretKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends DBConfig {

    public Connection getConnection() throws SQLException {
        String connectionSrt = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
        return DriverManager.getConnection(connectionSrt, DB_USER, DB_PASS);
    }


    // добавление нового пользователя в базу данных
    public void addNewUser(User user) {
        String request = "INSERT INTO " + User.Table.TABLE_NAME + " (" +
                User.Table.USER_ID + "," + User.Table.FIRST_NAME + "," + User.Table.LAST_NAME + "," +
                User.Table.USERNAME + "," + User.Table.SECRET_KEY + ")" + "VALUES(?,?,?,?,?)";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, user.getId());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.setString(5, Cryption.createSecretKeyString());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
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
        String request = "INSERT INTO " + PasswordRecord.Table.TABLE_NAME + " (" +
                PasswordRecord.Table.USER_ID + "," + PasswordRecord.Table.SERVICE_NAME + "," +
                PasswordRecord.Table.LOGIN + "," + PasswordRecord.Table.PASSWORD + "," +
                PasswordRecord.Table.COMMENT + ")" + "VALUES(?,?,?,?,?)";
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

    public String getUserSecretKey(long userId) {
        ResultSet resultSet = null;
        String request = "SELECT " + User.Table.SECRET_KEY +
                " FROM " + User.Table.TABLE_NAME + " WHERE " +
                User.Table.USER_ID + "=?";
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
                return resultSet.getString(User.Table.SECRET_KEY);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void changeRepoPass(long userId, String newPass) {
        String request = "UPDATE " + RepositoryPassword.Table.TABLE_NAME +
                " SET " + RepositoryPassword.Table.REPOSITORY_PASSWORD + "=?" +
                " WHERE " + RepositoryPassword.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setString(1, newPass);
            preparedStatement.setLong(2, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteRepo(long userId) {
        String request = "DELETE FROM " + User.Table.TABLE_NAME + " WHERE " +
                User.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<PasswordRecord> getUserPasswords(long userId) {
        ResultSet resultSet = null;
        String request = "SELECT " +
                PasswordRecord.Table.SERVICE_NAME + "," +
                PasswordRecord.Table.LOGIN + "," +
                PasswordRecord.Table.PASSWORD + "," +
                PasswordRecord.Table.COMMENT +
                " FROM " + PasswordRecord.Table.TABLE_NAME +
                " WHERE " + PasswordRecord.Table.USER_ID + "=?";
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<PasswordRecord> list = new ArrayList<>();

        try {
            assert resultSet != null;
            while (resultSet.next()) {
                PasswordRecord passwordRecord = new PasswordRecord();

                // получение
                String serviceName = resultSet.getString(PasswordRecord.Table.SERVICE_NAME);
                String login = resultSet.getString(PasswordRecord.Table.LOGIN);
                String password = resultSet.getString(PasswordRecord.Table.PASSWORD);
                String comment = resultSet.getString(PasswordRecord.Table.COMMENT);

                // Проблема с кодировкой: иногда в базу добавляется символ вопроса в черном ромбе,
                //  который корректно не расшифровывается!

                // расшифровка и заполнение
                SecretKey key = Cryption.getSecretKeyFromString(getUserSecretKey(userId));
                passwordRecord.setServiceName(Cryption.do_AESDecryption(serviceName.getBytes(), key));
                passwordRecord.setLogin(Cryption.do_AESDecryption(login.getBytes(), key));
                passwordRecord.setPassword(Cryption.do_AESDecryption(password.getBytes(), key));
                passwordRecord.setComment(Cryption.do_AESDecryption(comment.getBytes(), key));

                list.add(passwordRecord);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void deleteRecord(long userId, String serviceName) {
        String request = "DELETE FROM " + PasswordRecord.Table.TABLE_NAME +
                " WHERE " + PasswordRecord.Table.USER_ID + "=?" +
                " AND " + PasswordRecord.Table.SERVICE_NAME + "=?";

        String encodedServiceName = null;
        try {
            encodedServiceName = new String(Cryption.do_AESEncryption(serviceName,
                    Cryption.getSecretKeyFromString(getUserSecretKey(userId))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert encodedServiceName != null;
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, encodedServiceName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void editRecordPassword(long userId, String serviceName,  String newPassword) {
        String request = "UPDATE " + PasswordRecord.Table.TABLE_NAME +
                " SET " + PasswordRecord.Table.PASSWORD + "=?" +
                " WHERE " + PasswordRecord.Table.USER_ID + "=?" +
                " AND " + PasswordRecord.Table.SERVICE_NAME + "=?";

        String encodedNewPassword = null;
        String encodedServiceName = null;
        try {
            encodedNewPassword = new String(Cryption.do_AESEncryption(newPassword,
                    Cryption.getSecretKeyFromString(getUserSecretKey(userId))));
            encodedServiceName = new String(Cryption.do_AESEncryption(serviceName,
                    Cryption.getSecretKeyFromString(getUserSecretKey(userId))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert encodedNewPassword != null;
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setString(1, encodedNewPassword);
            preparedStatement.setLong(2, userId);
            preparedStatement.setString(3, encodedServiceName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void editRecordComment(long userId, String serviceName, String newComment) {
        String request = "UPDATE " + PasswordRecord.Table.TABLE_NAME +
                " SET " + PasswordRecord.Table.COMMENT + "=?" +
                " WHERE " + PasswordRecord.Table.USER_ID + "=?" +
                " AND " + PasswordRecord.Table.SERVICE_NAME + "=?";

        String encodedNewComment = null;
        String encodedServiceName = null;
        try {
            encodedNewComment = new String(Cryption.do_AESEncryption(newComment,
                    Cryption.getSecretKeyFromString(getUserSecretKey(userId))));
            encodedServiceName = new String(Cryption.do_AESEncryption(serviceName,
                    Cryption.getSecretKeyFromString(getUserSecretKey(userId))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert encodedNewComment != null;
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(request);
            preparedStatement.setString(1, encodedNewComment);
            preparedStatement.setLong(2, userId);
            preparedStatement.setString(3, encodedServiceName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
