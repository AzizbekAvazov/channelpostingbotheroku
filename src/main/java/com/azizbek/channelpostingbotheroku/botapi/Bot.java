package com.azizbek.channelpostingbotheroku.botapi;

import com.azizbek.channelpostingbotheroku.botapi.states.UpdateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class Bot extends TelegramWebhookBot {
    private final UpdateHandler updateHandler;
    @Value("${botUsername}")
    private String botUsername;
    @Value("${botToken}")
    private String botToken;
    @Value("${botPath}")
    private String botPath;

    public Bot(UpdateHandler updateHandler) {
        this.updateHandler = updateHandler;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(update.getMessage().getFrom().getId().toString());
        replyMessage.setText("YOU ARE THE BEST OF BESTS!!!");
        return replyMessage;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    public void sendMessage(SendMessage replyMessage) {
        try {
            execute(replyMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void forwardMessage(ForwardMessage forwardMessage) {
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public File getFile(GetFile getFile) {
        try {
            return execute(getFile);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMediaGroup(SendMediaGroup sendMediaGroup) {
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
