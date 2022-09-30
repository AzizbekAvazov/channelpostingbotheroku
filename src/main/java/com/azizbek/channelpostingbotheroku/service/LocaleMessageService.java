package com.azizbek.channelpostingbotheroku.service;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.ResourceBundle;

@Service
public class LocaleMessageService {
	private Locale locale;
	private ResourceBundle resourceBundle;
	
	public LocaleMessageService() {

	}
	
	public LocaleMessageService(Locale locale, ResourceBundle resourceBundle) {
		this.locale = locale;
		this.resourceBundle = resourceBundle;
	}

	public String getLocaleMessage(String lang, String msg) {
		if (lang==null) {
			lang="RU";
		}

		locale = new Locale(lang);

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames("lang/messages");
		messageSource.setDefaultEncoding("UTF-8");

		//resourceBundle = ResourceBundle.getBundle("messages", locale);
		
		return messageSource.getMessage(msg, null,locale);
	}
}
