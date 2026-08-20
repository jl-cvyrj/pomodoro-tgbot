package by.karalina.pomodoro.timer;

import by.karalina.pomodoro.bot.PomodoroBot;
import by.karalina.pomodoro.entity.UserSession;
import by.karalina.pomodoro.entity.UserSessionStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroTimer {

    public static final String TIMER_WORKING_STARTED = "TIMER_WORKING_STARTED";
    public static final String TIMER_BREAK_STARTED = "TIMER_BREAK_STARTED";
    public static final String TIMER_BIG_BREAK_STARTED = "TIMER_BIG_BREAK_STARTED";
    public static final String TIMER_BIG_BREAK_FINISHED = "TIMER_BIG_BREAK_FINISHED";

    private final ConcurrentHashMap<Long, UserSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService ticker = Executors.newScheduledThreadPool(1);

    final PomodoroBot pomodoroBot;

    public PomodoroTimer(@Lazy PomodoroBot pomodoroBot) {
        this.pomodoroBot = pomodoroBot;
        ticker.scheduleAtFixedRate(this::tickAllSessions, 0, 1, TimeUnit.SECONDS);
    }

    public UserSession getUserSession(long chatId) {
        return sessions.get(chatId);
    }

    public void startNewTimer(long chatId) {
        UserSession userSession = new UserSession();
        userSession.setChatId(chatId);
        userSession.setSessionStatus(UserSessionStatus.WORKING);
        userSession.setCurrentLeftTime(userSession.getWorkingTime());
        userSession.setCurrentCycle(1);

        sessions.put(chatId, userSession);
    }

    public void tickAllSessions() {
        for (UserSession userSession : sessions.values()) {
            int currentLeftTime = userSession.getCurrentLeftTime();
            if (currentLeftTime > 0) {
                userSession.setCurrentLeftTime(currentLeftTime - 1);

                if (userSession.getCurrentLeftTime() == 0) {
                    if (userSession.getSessionStatus() == UserSessionStatus.WORKING) {
                        if (userSession.getCurrentCycle() == 4) {
                            pomodoroBot.handleTimerEvent(userSession.getChatId(), TIMER_BIG_BREAK_STARTED);
                            userSession.setSessionStatus(UserSessionStatus.BIG_BREAK);
                            userSession.setCurrentLeftTime(userSession.getBigBreakTime());
                        } else {
                            pomodoroBot.handleTimerEvent(userSession.getChatId(), TIMER_BREAK_STARTED);
                            userSession.setSessionStatus(UserSessionStatus.BREAK);
                            userSession.setCurrentLeftTime(userSession.getBreakTime());
                        }
                    }
                    else if (userSession.getSessionStatus() == UserSessionStatus.BREAK) {
                        pomodoroBot.handleTimerEvent(userSession.getChatId(), TIMER_WORKING_STARTED);
                        userSession.setSessionStatus(UserSessionStatus.WORKING);
                        userSession.setCurrentLeftTime(userSession.getWorkingTime());
                        int cycle = userSession.getCurrentCycle();
                        userSession.setCurrentCycle(cycle + 1);
                    }
                    else if (userSession.getSessionStatus() == UserSessionStatus.BIG_BREAK) {
                        pomodoroBot.handleTimerEvent(userSession.getChatId(), TIMER_BIG_BREAK_FINISHED);
                        userSession.setSessionStatus(UserSessionStatus.OFF);
                        userSession.setCurrentLeftTime(-1);
                        userSession.setCurrentCycle(1);
                    }
                }
            }
        }
    }
}
