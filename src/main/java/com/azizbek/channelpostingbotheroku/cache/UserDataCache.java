package com.azizbek.channelpostingbotheroku.cache;

import com.azizbek.channelpostingbotheroku.botapi.BotState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

@Service
public class UserDataCache {
    private final Map<String, BotState> usersBotState = new HashMap<>();
    private final Map<String, UserData> userDataMap = new HashMap<>();

    public void setUsersCurrentBotState(String user_id, BotState botState) {
        usersBotState.put(user_id, botState);
    }

    public BotState getUsersCurrentBotState(String user_id) {
        BotState botState = usersBotState.get(user_id);
        if (botState == null) {
            botState = BotState.START;
        }

        return botState;
    }

    public void saveUserDataMap(String user_id, UserData userData) {
        userDataMap.put(user_id, userData);
    }

    public UserData getUserDataMap(String user_id) {
        UserData paymentsData = userDataMap.get(user_id);
        if (paymentsData == null) {
            paymentsData = new UserData();
        }
        return paymentsData;
    }

}
