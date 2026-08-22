package by.karalina.pomodoro.config;

import by.karalina.pomodoro.bot.PomodoroBot;
import by.karalina.pomodoro.timer.PomodoroTimer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

@Configuration
@AllArgsConstructor
public class BotConfig {

    private final TelegramConfig telegramConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    public PomodoroBot springWebhookBot(SetWebhook setWebhook, PomodoroTimer pomodoroTimer, List<BotCommand> menu) {
        return new PomodoroBot(
                setWebhook,
                pomodoroTimer,
                menu,
                telegramConfig.getWebhookPath(),
                telegramConfig.getBotUsername(),
                telegramConfig.getBotToken()
        );
    }

    @Bean
    public List<BotCommand> createBotMenu() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "get a welcome message"));
        commands.add(new BotCommand("/start_timer", "start the timer with default values"));
        commands.add(new BotCommand("/see_timer_values", "get info about current timer values"));
        commands.add(new BotCommand("/change_timer_values", "change current timer values"));
        commands.add(new BotCommand("/help", "get more info about all commands in bot"));

        return commands;
    }
}