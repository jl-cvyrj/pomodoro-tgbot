package by.karalina.pomodoro;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
public class PomodoroApplication {

    public static void main(String[] args) {
        SpringApplication.run(PomodoroApplication.class, args);
        log.info("Service PomodoroBy is launched!");
    }
}
