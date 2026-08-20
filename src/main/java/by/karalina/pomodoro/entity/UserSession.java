package by.karalina.pomodoro.entity;

import lombok.Data;

@Data
public class UserSession {

    long chatId;
    UserSessionStatus sessionStatus;
    int workingTime = 1500;
    int breakTime = 300;
    int bigBreakTime = 1200;
    int currentLeftTime;
    int currentCycle;
}
