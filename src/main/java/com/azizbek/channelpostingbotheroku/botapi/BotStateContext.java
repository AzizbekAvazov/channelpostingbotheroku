package com.azizbek.channelpostingbotheroku.botapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

@Component
public class BotStateContext {
    private Map<BotState, InputMessageHandler> messageHandlers = new HashMap<>();

    public BotStateContext(List<InputMessageHandler> messageHandlers) {
        messageHandlers.forEach(handler -> this.messageHandlers.put(handler.getHandlerName(), handler));
    }

    public SendMessage processInputMessage(BotState currentState, Message message) {
        InputMessageHandler currentMessageHandler = findMessageHandler(currentState);
        if (currentMessageHandler == null) {
            currentMessageHandler = messageHandlers.get(BotState.START);
        }
        return currentMessageHandler.handle(message);
    }

    private InputMessageHandler findMessageHandler(BotState currentState) {
        if (isStartState(currentState)) {
            return messageHandlers.get(BotState.START);
        }
        if (isNewPostState(currentState)) {
            return messageHandlers.get(BotState.NEW_POST);
        }
        if (isChangeLangState(currentState)){
            return messageHandlers.get(BotState.CHANGE_LANG);
        }

        return messageHandlers.get(currentState);
    }

    private boolean isStartState(BotState currentState) {
        switch (currentState) {
            case START:
                return true;
            default:
                return false;
        }
    }

    private boolean isChangeLangState(BotState currentState) {
        switch (currentState) {
            case CHANGE_LANG:
                return true;
            default:
                return false;
        }
    }

    private boolean isNewPostState(BotState currentState) {
        switch (currentState) {
            case NEW_POST:
            case GET_AD_TITLE:
            case GET_DESCRIPTION:
            case GET_PHONE_NUMBER:
            case GET_IMAGES:
            case CONFIRM_AD:
                return true;
            default:
                return false;
        }
    }
}
