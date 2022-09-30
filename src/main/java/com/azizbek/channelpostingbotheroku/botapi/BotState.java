package com.azizbek.channelpostingbotheroku.botapi;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

public enum BotState {
    START,
    CHANGE_LANG,
    NEW_POST,
    GET_AD_TITLE,
    GET_DESCRIPTION,
    GET_PHONE_NUMBER,
    GET_IMAGES,
    CONFIRM_AD,
}
