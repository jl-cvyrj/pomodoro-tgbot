package by.karalina.pomodoro.bot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Getter
@Setter
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroBot extends SpringWebhookBot {

    String botPath;
    String botUsername;
    String botToken;

    public PomodoroBot(SetWebhook setWebhook, String botPath, String botUsername, String botToken) {
        super(setWebhook, botToken);
        this.botPath = botPath;
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            log.info("Received message from {}: {}", chatId, userText);
            SendMessage reply = new SendMessage();
            reply.setChatId(String.valueOf(chatId));
            reply.setText("Hi! Here will be Belarusian pomodoro someday. You texted: " + userText);

            try {
                execute(reply);
            } catch (Exception e) {
                log.error("Error sending message: ", e);
            }
        }
        return null;
    }
}
