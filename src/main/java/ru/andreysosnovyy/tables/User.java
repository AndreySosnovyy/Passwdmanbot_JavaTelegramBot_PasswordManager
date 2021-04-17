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

    public static class Table {
        public static final String TABLE_NAME = "users_info";
        public static final String USER_ID = "user_id";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String USERNAME = "username";
    }

}

