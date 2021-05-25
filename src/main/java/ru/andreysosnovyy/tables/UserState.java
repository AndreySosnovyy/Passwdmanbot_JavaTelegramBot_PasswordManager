package ru.andreysosnovyy.tables;

import lombok.Getter;
import lombok.Setter;
import ru.andreysosnovyy.DBHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@Getter
@Setter
public class UserState {

    User user;              // пользователь, с которым идет диалог
    String workerName;      // имя воркера, который должен обработать запрос
    Date datetime;          // время последнего запроса пользователя

    // конструктор для результата запроса из базы данных
    public UserState(ResultSet resultSet) {
        DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
        ResultSet userResultSet = null;
        try {
            if (resultSet.next()) {
                userResultSet = handler.getUser(resultSet.getLong(Table.USER_ID)); // поиск пользователя в базе
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        User user = null;
        try {
            assert userResultSet != null;
            if (userResultSet.next()) {
                user = User.builder()
                        .id(userResultSet.getLong(User.Table.USER_ID))
                        .firstName(userResultSet.getString(User.Table.FIRST_NAME))
                        .lastName(userResultSet.getString(User.Table.LAST_NAME))
                        .username(userResultSet.getString(User.Table.USERNAME))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.user = user;

        try {
            this.datetime = resultSet.getTimestamp(Table.TIME_OF_LAST_REQUEST);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            this.workerName = resultSet.getString(Table.STATE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


//    public boolean CheckTimeout(int shift) {
//        // shift - разница между временем последнего обращения и текущим (в секундах) для проверки
//    }


    public static class Table {
        public static final String TABLE_NAME = "users_states";
        public static final String USER_ID = "user_id";
        public static final String TIME_OF_LAST_REQUEST = "time_of_last_request";
        public static final String STATE = "state";
    }

    public static class Names {
        public static final String BASE = "base"; // начальное состояние с клавиатурой
        public static final String BASE_NO_REPOSITORY_PASSWORD = "base_no_repository_password"; // еще не добавлен мастер-пароль

        public static final String REPOSITORY_PASS = "repository_pass"; // ожидается ввод пароля от репозитория
        public static final String REPOSITORY_LIST = "repository_list"; // активная сессия работы с хранилищем (просмотр)

//        public static final String SETTINGS = "settings";
    }
}
