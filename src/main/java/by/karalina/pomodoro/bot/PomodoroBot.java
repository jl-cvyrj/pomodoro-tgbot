package by.karalina.pomodoro.bot;

import by.karalina.pomodoro.entity.User;
import by.karalina.pomodoro.timer.PomodoroTimer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PomodoroBot extends SpringWebhookBot {

    public static final String TIMER_WORKING_STARTED = "TIMER_WORKING_STARTED";
    public static final String TIMER_BREAK_STARTED = "TIMER_BREAK_STARTED";
    public static final String TIMER_BIG_BREAK_STARTED = "TIMER_BIG_BREAK_STARTED";
    public static final String TIMER_BIG_BREAK_FINISHED = "TIMER_BIG_BREAK_FINISHED";

    public static final String MESSAGE_WORKING_STARTED = "So now it's time to work! Go on bro";
    public static final String MESSAGE_BREAK_STARTED = "Hey! You are doing great! Now have some rest tiger";
    public static final String MESSAGE_BIG_BREAK_STARTED = "Are you nuts?? Now have a deserved break!";
    public static final String MESSAGE_BIG_BREAK_FINISHED = "Would you like to start another cycle of crazy work?";

    final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();
    final PomodoroTimer pomodoroTimer;
    final List<BotCommand> botMenu;

    String botPath;
    String botUsername;
    String botToken;

    public PomodoroBot(SetWebhook setWebhook, PomodoroTimer pomodoroTimer, List<BotCommand> botMenu, String botPath, String botUsername, String botToken) {
        super(setWebhook, botToken);
        this.pomodoroTimer = pomodoroTimer;
        this.botMenu = botMenu;
        this.botPath = botPath;
        this.botUsername = botUsername;
        this.botToken = botToken;

        registerMenu(botMenu);
    }

    public User getUser(long chatId) {
        if (users.get(chatId) == null) {
           User user = new User();
           user.setChatId(chatId);
           users.put(chatId, user);
        }
        return users.get(chatId);
    }

    public void registerMenu(List<BotCommand> menu) {
        try {
            this.execute(new SetMyCommands(menu, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error setting bot menu: {}", e.getMessage());
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            log.info("Received message from {}: {}", chatId, messageText);
            User user;
            switch (messageText) {
                case "/start":
                    sendTelegramMessage(chatId,"""
                        Hi! I'm Pomodoro bot!
                        I'm here to help you to organize your time.
                        You can check the list of the options in menu below.
                        To get more information about all my commands, send /help
                        """);
                    break;

                case "/start_timer":
                    user = getUser(chatId);
                    sendTelegramMessage(chatId, "Pomodoro Timer is started!");
                    pomodoroTimer.startNewTimer(user);
                    break;

                case "/see_timer_values":
                    user = getUser(chatId);
                    int WorkTimeMin = user.getWorkingTime() / 60;
                    int WorkTimeSec = user.getWorkingTime() % 60;
                    int BreakTimeMin = user.getBreakTime() / 60;
                    int BreakTimeSec = user.getBreakTime() % 60;
                    int BigBreakTimeMin = user.getBigBreakTime() / 60;
                    int BigBreakTimeSec = user.getBigBreakTime() % 60;
                    sendTelegramMessage(chatId, """
                            Here are current timer values:
                            Working time is %d m %d s.
                            Break time is %d m %d s.
                            Big break time is %d m %d s.
                            """.formatted(WorkTimeMin, WorkTimeSec, BreakTimeMin,
                            BreakTimeSec, BigBreakTimeMin, BigBreakTimeSec));
                    break;

                case "/change_timer_values":
                    changeTimerValues(chatId);
                    break;

                case "/help":
                    changeTimerValues(chatId);
                    break;

                default:
                    sendTelegramMessage(chatId,"Unknown command. Please, sent /start");
                    break;
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
                    sendTelegramMessage(chatId, "Enter new working time in minutes:");
                    break;
                case "BREAK_TIME_BUTTON":
                    sendTelegramMessage(chatId, "Enter new break time in minutes:");
                    break;
                case "BIGBREAK_TIME_BUTTON":
                    sendTelegramMessage(chatId, "Enter new big break time in minutes:");
                    break;
                default:
                    sendTelegramMessage(chatId, "Unknown callback");
                    break;
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

    public void sendTelegramMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageText);
        try {
            execute(message);
        } catch (Exception e) {
            log.error("Error sending message: ", e);
        }
    }

    public void handleTimerEvent(long chatId, String eventMessage) {
        switch (eventMessage) {
            case TIMER_WORKING_STARTED:
                sendTelegramMessage(chatId, MESSAGE_WORKING_STARTED);
                break;
            case TIMER_BREAK_STARTED:
                sendTelegramMessage(chatId, MESSAGE_BREAK_STARTED);
                break;
            case TIMER_BIG_BREAK_STARTED:
                sendTelegramMessage(chatId, MESSAGE_BIG_BREAK_STARTED);
                break;
            case TIMER_BIG_BREAK_FINISHED:
                sendTelegramMessage(chatId, MESSAGE_BIG_BREAK_FINISHED);
                break;
        }
    }
}
