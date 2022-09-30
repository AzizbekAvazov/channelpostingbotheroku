package com.azizbek.channelpostingbotheroku.botapi;

import com.azizbek.channelpostingbotheroku.cache.UserData;
import com.azizbek.channelpostingbotheroku.cache.UserDataCache;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

@Component
public class CallbackQueryHandler {
    private final Bot bot;
    private final UserDataCache userDataCache;

    public CallbackQueryHandler(@Lazy Bot bot, UserDataCache userDataCache) {
        this.bot = bot;
        this.userDataCache = userDataCache;
    }

    public SendMessage processCallbackQuery(CallbackQuery callbackQuery) {
        String user_id = callbackQuery.getFrom().getId().toString();
        String callbackData = callbackQuery.getData();

        SendMessage replyMessage = null;

        switch (callbackData) {
            case "confirmChannelPost": {
                replyMessage = confirmPost(user_id, callbackQuery.getMessage().getMessageId());
                break;
            }
        }

        return replyMessage;
    }

    public SendMessage confirmPost(String user_id, int message_id) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setText("✅ Joylandi !");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton sendMessageButton = new KeyboardButton();
        sendMessageButton.setText(EmojiParser.parseToUnicode("E'lon yaratish:exclamation:"));

        keyboardRow.add(sendMessageButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        deleteLastMessage(user_id, message_id);
        postToChannel(user_id, userDataCache.getUserDataMap(user_id).getText());

        return replyMessage;
    }

    private void deleteLastMessage(String user_id, int message_id) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(message_id);
        deleteMessage.setChatId(user_id);
        bot.deleteMessage(deleteMessage);
    }

    private void postToChannel(String user_id, String text) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        if (userData.isImage()) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId("-1001645249483");
            sendPhoto.setCaption(EmojiParser.parseToUnicode(text));
            sendPhoto.setParseMode(ParseMode.HTML);

            java.io.File imagePath = userData.getLatestUserImage();

            InputFile inputFile = new InputFile();
            inputFile.setMedia(imagePath);
            sendPhoto.setPhoto(inputFile);

            bot.sendPhoto(sendPhoto);
        } else {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setText(text);
            replyMessage.setChatId("-1001645249483");//("-1001760318604");
            bot.sendMessage(replyMessage);
        }
    }
}
