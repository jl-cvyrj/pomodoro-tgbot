package by.karalina.pomodoro.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramConfig {

    @Value("${WEBHOOK}")
    String webhookPath;

    @Value("${BOT_NAME}")
    String botUsername;

    @Value("${BOT_TOKEN}")
    String botToken;
}
