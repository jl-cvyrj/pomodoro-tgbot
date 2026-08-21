package by.karalina.pomodoro.entity;

import lombok.Data;

@Data
public class UserSession {

    long chatId;
    UserSessionStatus sessionStatus;
    int workingTime;
    int breakTime;
    int bigBreakTime;
    int currentLeftTime;
    int currentCycle;
}
