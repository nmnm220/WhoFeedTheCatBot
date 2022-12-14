package io.proj3ct.WhoFeedTheCatBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.proj3ct.WhoFeedTheCatBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.Date;


@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    final BotConfig config;
    @Value("${bot.chat.id}")
    private long chatId;
    private boolean catFed = false;
    private final String catName = "Тиша";

    private final String catNameDec = catName.substring(0, catName.length() - 1) + 'у';

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        /*if (update.getMessage().getText().equals("test"))
        {
            long chatId = update.getMessage().getChatId();
            sendMessage(chatId, String.valueOf(chatId));
        }*/

        if (update.hasMessage() && update.getMessage().hasText() & parseMessage(update.getMessage().getText())) {
            long chatId = update.getMessage().getChatId();
            if (!catFed) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("k:mm");
                Date currDate = new Date();


                sendMessage(chatId, update.getMessage().getFrom().getFirstName() + " покормил(а) меня в " + simpleDateFormat.format(currDate) + "." +
                        "\nУра! Теперь я сыт.");
                catFed = true;
                this.chatId = update.getMessage().getChatId();
            } else {

                //String catNameDec = catName.substring(0, catName.length() - 1) + 'у';
                sendMessage(chatId,  "Меня уже покормили раньше.");
            }
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private boolean parseMessage(String message) {
        if (message != null) {
            message = message.toLowerCase();
            return message.contains("я покормил");
        }
        return false;
    }

    @Scheduled(cron = "0 0 5,14,22 * * *")
    private void feedReminder() {
        catFed = false;
        if (chatId != 0) {
            String messageToSend = "Я хочу кушать, покормите меня!";
            sendMessage(chatId, messageToSend);
        }
    }

    /*@Scheduled(cron = "0 30 * * * *")
    private void checkCatFed() {
        if (!catFed) {
            String messageToSend = EmojiParser.parseToUnicode("Похоже " + catNameDec + " никто не покормил " + ":pleading_face:");
            sendMessage(chatId ,messageToSend);
        }
    }*/
}
