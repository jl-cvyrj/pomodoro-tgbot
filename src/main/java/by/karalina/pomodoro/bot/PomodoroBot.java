package by.karalina.pomodoro.bot;

import by.karalina.pomodoro.timer.PomodoroTimer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroBot extends SpringWebhookBot {

    String botPath;
    String botUsername;
    String botToken;

    PomodoroTimer pomodoroTimer = new PomodoroTimer();

    public PomodoroBot(SetWebhook setWebhook, String botPath, String botUsername, String botToken) {
        super(setWebhook, botToken);
        this.botPath = botPath;
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            log.info("Received message from {}: {}", chatId, messageText);

            SendMessage reply = new SendMessage();
            reply.setChatId(chatId);
            boolean needToSendReply = true;

            switch (messageText) {
                case "/start":
                    reply.setText("""
                        Hi! I'm Pomodoro bot!
                        I'm here to help you to organize your time.
                        Please, choose the option below.
                        """);
                    break;

                case "/start the default timer":
                    pomodoroTimer.startTimer();
                    reply.setText("Timer is started!");
                    break;

                case "/see timer values":
                    int WorkTimeMin = pomodoroTimer.getWorkTime() / 60000;
                    int WorkTimeSec = pomodoroTimer.getWorkTime() % 60000;
                    int BreakTimeMin = pomodoroTimer.getBreakTime() / 60000;
                    int BreakTimeSec = pomodoroTimer.getBreakTime() % 60000;
                    int BigBreakTimeMin = pomodoroTimer.getBigBreakTime() / 60000;
                    int BigBreakTimeSec = pomodoroTimer.getBigBreakTime() % 60000;
                    reply.setText("""
                            Here are current timer values:
                            Working time is %d m %d s.
                            Break time is %d m %d s.
                            Big break time is %d m %d s.
                            """.formatted(WorkTimeMin, WorkTimeSec, BreakTimeMin,
                            BreakTimeSec, BigBreakTimeMin, BigBreakTimeSec));
                    break;

                case "/change default timer values":
                    needToSendReply = false;
                    changeTimerValues(chatId);
                    break;

                default:
                    reply.setText("Unknown command. Please, sent /start");
                    break;
            }

            if (needToSendReply) {
                try {
                    execute(reply);
                } catch (Exception e) {
                    log.error("Error sending message: ", e);
                }
            }
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            log.info("Received callback: {} from {}", callbackData, chatId);

            SendMessage message = new SendMessage();
            message.setChatId(chatId);

            switch (callbackData) {
                case "WORKING_TIME_BUTTON":
                    message.setText("Enter new working time in minutes:");
                    break;
                case "BREAK_TIME_BUTTON":
                    message.setText("Enter new break time in minutes:");
                    break;
                case "BIGBREAK_TIME_BUTTON":
                    message.setText("Enter new big break time in minutes:");
                    break;
                default:
                    message.setText("Unknown callback");
                    break;
            }
            try {
                execute(message);
            } catch (Exception e) {
                log.error("Error sending message: ", e);
            }
        }
        return null;
    }

    private void changeTimerValues(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose value to change");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        InlineKeyboardButton workingTimeButton = new InlineKeyboardButton();
        InlineKeyboardButton breakTimeButton = new InlineKeyboardButton();
        InlineKeyboardButton bigBreakTimeButton = new InlineKeyboardButton();

        workingTimeButton.setText("Change working time");
        workingTimeButton.setCallbackData("WORKING_TIME_BUTTON");

        breakTimeButton.setText("Change break time");
        breakTimeButton.setCallbackData("BREAK_TIME_BUTTON");

        bigBreakTimeButton.setText("Change big break time");
        bigBreakTimeButton.setCallbackData("BIGBREAK_TIME_BUTTON");

        rowInLine.add(workingTimeButton);
        rowInLine.add(breakTimeButton);
        rowInLine.add(bigBreakTimeButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);
        } catch (Exception e) {
            log.error("Error sending message: ", e);
        }
    }
}
