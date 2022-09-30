package com.azizbek.channelpostingbotheroku.botapi.states;

import com.azizbek.channelpostingbotheroku.botapi.BotState;
import com.azizbek.channelpostingbotheroku.botapi.BotStateContext;
import com.azizbek.channelpostingbotheroku.botapi.CallbackQueryHandler;
import com.azizbek.channelpostingbotheroku.cache.UserData;
import com.azizbek.channelpostingbotheroku.cache.UserDataCache;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

@Component
public class UpdateHandler {
    private final UserDataCache userDataCache;
    private final BotStateContext botStateContext;
    private final CallbackQueryHandler callbackQueryHandler;

    public UpdateHandler(UserDataCache userDataCache, BotStateContext botStateContext,
                         CallbackQueryHandler callbackQueryHandler) {
        this.userDataCache = userDataCache;
        this.botStateContext = botStateContext;
        this.callbackQueryHandler = callbackQueryHandler;
    }

    public SendMessage processUpdate(Update update) {
        SendMessage replyMessage = null;
        if (update.hasMessage()) {
            replyMessage = processMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            replyMessage = callbackQueryHandler.processCallbackQuery(update.getCallbackQuery());
        }

        return replyMessage;
    }

    private SendMessage processMessage(Message message) {
        SendMessage replyMessage;
        String user_id = message.getFrom().getId().toString();
        UserData userData = userDataCache.getUserDataMap(user_id);
        BotState botState = BotState.START;

        if (message.hasText() || message.hasPhoto()) {
            String text = message.getText();

            if (text == null && message.hasPhoto()) {
                text = "image";
            }

            switch (text) {
                case "/start":
                    botState = BotState.START;
                    /*replyMessage = startingMessage(message);*/
                    break;
                case "Изменить язык \uD83C\uDF10":
                case "Tilni ōzgartirish \uD83C\uDF10":
                    botState = BotState.CHANGE_LANG;
                    break;
                case "E'lon yaratish❗":
                case "Создать обьявление❗":
                    if (userData.getLang() == null) {
                        botState = BotState.START;
                    } else {
                        botState = BotState.NEW_POST;
                    }
                    break;
                default:
                    botState = userDataCache.getUsersCurrentBotState(user_id);
                    break;
            }
        } else if (message.hasContact()) {
            botState = userDataCache.getUsersCurrentBotState(user_id);
            message.setText("contact");
        }

        userDataCache.setUsersCurrentBotState(user_id, botState);
        replyMessage = botStateContext.processInputMessage(botState, message);
        return replyMessage;
    }
}
