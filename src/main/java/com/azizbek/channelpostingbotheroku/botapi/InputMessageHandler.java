package com.azizbek.channelpostingbotheroku.botapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public interface InputMessageHandler {
	SendMessage handle(Message message);
	BotState getHandlerName();
}
