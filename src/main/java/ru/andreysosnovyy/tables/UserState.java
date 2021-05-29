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
        public static final String REPOSITORY_SEARCH = "repository_search"; // ожидание ввода слова для поиска среди названий сервисов
        public static final String REPOSITORY_ADD_SERVICE_NAME = "repository_add_service_name"; // ожидание ввода названия сервиса
        public static final String REPOSITORY_ADD_LOGIN = "repository_add_login"; // ожидание ввода логина
        public static final String REPOSITORY_ADD_PASSWORD = "repository_add_password"; // ожидание ввода пароля
        public static final String REPOSITORY_ADD_COMMENT = "repository_add_comment"; // ожидание ввода комментария

        public static final String SETTINGS = "settings"; // ожидание четкой инструкции настроек
        public static final String SETTINGS_DELETE_REPO = "settings_delete_repo"; // надо подтвердить удаление
        public static final String SETTINGS_CHANGE_MASTER_PASS_CONFIRM = "settings_change_master_pass_confirm"; // подтвердить мастер-пароль
        public static final String SETTINGS_RESTORE_PASS = "settings_restore_pass"; // пока нет
    }
}
