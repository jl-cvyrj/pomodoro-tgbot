package by.karalina.pomodoro.config;

import by.karalina.pomodoro.bot.PomodoroBot;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Configuration
@AllArgsConstructor
public class BotConfig {

    private final TelegramConfig telegramConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    public PomodoroBot springWebhookBot(SetWebhook setWebhook) {
        return new PomodoroBot(setWebhook, telegramConfig.getWebhookPath(), telegramConfig.getBotUsername(), telegramConfig.getBotToken());
    }
}