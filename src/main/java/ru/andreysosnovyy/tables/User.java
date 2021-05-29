package ru.andreysosnovyy.tables;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class User {

    private final long id;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String key;

    public static class Table {
        public static final String TABLE_NAME = "users";
        public static final String USER_ID = "user_id";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String USERNAME = "username";
        public static final String SECRET_KEY = "secret_key";
    }

}

