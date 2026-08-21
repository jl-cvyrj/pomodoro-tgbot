package by.karalina.pomodoro.entity;

import lombok.Data;

@Data
public class User {

    long chatId;
    int workingTime = 15;
    int breakTime = 3;
    int bigBreakTime = 12;
}
