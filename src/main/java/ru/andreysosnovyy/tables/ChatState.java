package ru.andreysosnovyy.tables;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.andreysosnovyy.workers.Worker;

@Builder
@Getter
@Setter
public class ChatState {

//    java.util.Date myDate = ...
//    java.sql.Time theTime = new java.sql.Time(myDate.getTime());
//    PreparedStatement pstmt = ...
//    pstmt.setTime(1, theTime);

    User user;
    Worker state;
    Date date;

//    public boolean CheckTimeout(int shift) {
//        // shift - разница между временем последнего обращения и текущим (в секундах) для проверки
//
//    }
}
