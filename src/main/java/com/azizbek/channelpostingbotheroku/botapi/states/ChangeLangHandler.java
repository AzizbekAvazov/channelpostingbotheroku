package com.azizbek.channelpostingbotheroku.botapi.states;

import com.azizbek.channelpostingbotheroku.botapi.Bot;
import com.azizbek.channelpostingbotheroku.botapi.BotState;
import com.azizbek.channelpostingbotheroku.botapi.InputMessageHandler;
import com.azizbek.channelpostingbotheroku.cache.UserData;
import com.azizbek.channelpostingbotheroku.cache.UserDataCache;
import com.azizbek.channelpostingbotheroku.service.LocaleMessageService;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.context.annotation.Lazy;
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
 * @created 24/04/2022
 */

@Component
public class ChangeLangHandler implements InputMessageHandler {
    private final String CHOOSE_LANG_MSG = ":uz: Iltimos tilni tanlang !\n\n:ru: Пожалуйста выберите язык !";

    private final UserDataCache userDataCache;
    private final LocaleMessageService localeMessageService;
    private final Bot bot;
    private final StartStateHandler startStateHandler;

    public ChangeLangHandler(UserDataCache userDataCache, LocaleMessageService localeMessageService,
                             @Lazy Bot bot, StartStateHandler startStateHandler) {
        this.userDataCache = userDataCache;
        this.localeMessageService = localeMessageService;
        this.bot = bot;
        this.startStateHandler = startStateHandler;
    }

    @Override
    public SendMessage handle(Message message) {
        return processMessage(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.CHANGE_LANG;
    }

    private SendMessage processMessage(Message message) {
        String text = message.getText();
        String user_id = message.getFrom().getId().toString();

        SendMessage replyMessage = null;
        UserData userData = userDataCache.getUserDataMap(user_id);

        switch (text) {
            case "Tilni ōzgartirish \uD83C\uDF10":
            case "Изменить язык \uD83C\uDF10":
                replyMessage = showLangOptions(user_id);
                break;
            case "\uD83C\uDDFA\uD83C\uDDFF Ōzbek tili":
                userData.setLang("UZ");
                userDataCache.saveUserDataMap(user_id, userData);
                bot.sendMessage(langChangeSuccessMsg(user_id));
                replyMessage = startStateHandler.startingMessage(user_id);
                break;
            case "\uD83C\uDDF7\uD83C\uDDFA Русский язык":
                userData.setLang("RU");
                userDataCache.saveUserDataMap(user_id, userData);
                bot.sendMessage(langChangeSuccessMsg(user_id));
                replyMessage = startStateHandler.startingMessage(user_id);
                break;
            case "Orqaga qaytish \uD83D\uDD19":
            case "Назад \uD83D\uDD19":
                replyMessage = startStateHandler.startingMessage(user_id);
                break;
        }
        return replyMessage;
    }

    private SendMessage showLangOptions(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);

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

        KeyboardRow keyboardRow3 = new KeyboardRow();
        KeyboardButton returnBack = new KeyboardButton();
        returnBack.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "return_back")));
        keyboardRow3.add(returnBack);

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        keyboardRowList.add(keyboardRow3);

        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendMessage langChangeSuccessMsg(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);

        replyMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "lang_change_success_msg")));

        return replyMessage;
    }
}
