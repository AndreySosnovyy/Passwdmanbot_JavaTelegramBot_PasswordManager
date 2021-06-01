package ru.andreysosnovyy.tables;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecord {

    private String serviceName;
    private String login;
    private String password;
    private String comment;


    public static class Table {
        public static final String TABLE_NAME = "passwords";
        public static final String USER_ID = "user_id";
        public static final String SERVICE_NAME = "service_name";
        public static final String LOGIN = "login";
        public static final String PASSWORD = "password";
        public static final String COMMENT = "comment";
    }
}
