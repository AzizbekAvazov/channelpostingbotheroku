package com.azizbek.channelpostingbotheroku;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Bot extends TelegramWebhookBot {
    @Value("${botUsername}")
    private String botUsername;
    @Value("${botToken}")
    private String botToken;
    @Value("${botPath}")
    private String botPath;

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
        replyMessage.setText("HELLLLLLOOOOOO!!!");
        return replyMessage;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
