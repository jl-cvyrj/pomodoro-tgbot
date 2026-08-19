package by.karalina.pomodoro.timer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroTimer {

    Integer WorkTime = 1500000;
    Integer BreakTime = 300000;
    Integer BigBreakTime = 1200000;

    public void startTimer() {

    }
}
