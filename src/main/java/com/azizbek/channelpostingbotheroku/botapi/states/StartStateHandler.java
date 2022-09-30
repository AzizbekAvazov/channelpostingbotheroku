package com.azizbek.channelpostingbotheroku.botapi.states;


import com.azizbek.channelpostingbotheroku.botapi.BotState;
import com.azizbek.channelpostingbotheroku.botapi.InputMessageHandler;
import com.azizbek.channelpostingbotheroku.cache.UserData;
import com.azizbek.channelpostingbotheroku.cache.UserDataCache;
import com.azizbek.channelpostingbotheroku.service.LocaleMessageService;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
public class StartStateHandler implements InputMessageHandler {
    private final String CHOOSE_LANG_MSG = ":uz: Iltimos tilni tanlang !\n\n:ru: Пожалуйста выберите язык !";
    private final UserDataCache userDataCache;
    private final LocaleMessageService localeMessageService;

    public StartStateHandler(UserDataCache userDataCache, LocaleMessageService localeMessageService) {
        this.userDataCache = userDataCache;
        this.localeMessageService = localeMessageService;
    }

    @Override
    public SendMessage handle(Message message) {
        return processMessage(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.START;
    }

    private SendMessage processMessage(Message message) {
        SendMessage replyMessage = null;
        String user_id = message.getFrom().getId().toString();
        String text = message.getText();

        if (text == null) {
            text = "/start";
        }

        UserData userData = userDataCache.getUserDataMap(user_id);

        switch (text) {
            case "/start":
                if (userData.getLang() == null) {
                    replyMessage = chooseLanguage(user_id);
                } else {
                    replyMessage = startingMessage(user_id);
                }
                break;
            case "\uD83C\uDDFA\uD83C\uDDFF Ōzbek tili":
                setUserLanguage(user_id, "UZ");
                replyMessage = startingMessage(user_id);
                break;
            case "\uD83C\uDDF7\uD83C\uDDFA Русский язык":
                setUserLanguage(user_id, "RU");
                replyMessage = startingMessage(user_id);
                break;
            default:
                if (userData.getLang() == null) {
                    replyMessage = chooseLanguage(user_id);
                }
        }

        return replyMessage;
    }

    private SendMessage chooseLanguage(String user_id) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        String replyMessageText = CHOOSE_LANG_MSG;
        replyMessage.setText(EmojiParser.parseToUnicode(replyMessageText));
        replyMessage.setChatId(user_id);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton uz = new KeyboardButton();
        uz.setText(EmojiParser.parseToUnicode(":uz: Ōzbek tili"));
        keyboardRow1.add(uz);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton ru = new KeyboardButton();
        ru.setText(EmojiParser.parseToUnicode(":ru: Русский язык"));
        keyboardRow2.add(ru);

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private void setUserLanguage(String user_id, String lang) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        userData.setLang(lang);
        userDataCache.saveUserDataMap(user_id, userData);
    }

    protected SendMessage startingMessage(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        String replyMessageText = localeMessageService.getLocaleMessage(userData.getLang(), "starting_msg");
        replyMessage.setText(EmojiParser.parseToUnicode(replyMessageText));
        replyMessage.setChatId(user_id);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton sendMessageButton = new KeyboardButton();
        sendMessageButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "create_ad")));
        keyboardRow1.add(sendMessageButton);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton changeLangButton = new KeyboardButton();
        changeLangButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "change_lang")));
        keyboardRow2.add(changeLangButton);

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }
}
