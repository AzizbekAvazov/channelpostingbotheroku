package com.azizbek.channelpostingbotheroku.botapi.states;

import com.azizbek.channelpostingbotheroku.botapi.Bot;
import com.azizbek.channelpostingbotheroku.botapi.BotState;
import com.azizbek.channelpostingbotheroku.botapi.InputMessageHandler;
import com.azizbek.channelpostingbotheroku.cache.UserData;
import com.azizbek.channelpostingbotheroku.cache.UserDataCache;
import com.azizbek.channelpostingbotheroku.service.LocaleMessageService;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

@Component
public class NewPostStateHandler implements InputMessageHandler {
    private final UserDataCache userDataCache;
    private final StartStateHandler startStateHandler;
    private final Bot bot;
    private final LocaleMessageService localeMessageService;

    @Value("${botToken}")
    private String botToken;

    public NewPostStateHandler(UserDataCache userDataCache, StartStateHandler startStateHandler, @Lazy Bot bot,
                               LocaleMessageService localeMessageService) {
        this.userDataCache = userDataCache;
        this.startStateHandler = startStateHandler;
        this.bot = bot;
        this.localeMessageService = localeMessageService;
    }

    @Override
    public SendMessage handle(Message message) {
        String messageText = message.getText();
        String user_id = message.getFrom().getId().toString();

        if (messageText != null) {
            if (messageText.equals("Bekor qilish \uD83D\uDEAB") || messageText.equals("Отмена \uD83D\uDEAB")) {
                userDataCache.setUsersCurrentBotState(user_id, BotState.START);
                return startStateHandler.startingMessage(user_id);
            }
        }
        return processMessage(message);
    }

    @Override
    public BotState getHandlerName() {
        return BotState.NEW_POST;
    }

    private SendMessage processMessage(Message message) {
        String messageText = message.getText();
        String user_id = message.getFrom().getId().toString();
        UserData userData = userDataCache.getUserDataMap(user_id);

        if (messageText == null) {
            messageText = "image";
            userData.setImage(true);
        } else {
            userData.setImage(false);
        }

        userDataCache.saveUserDataMap(user_id,userData);

        BotState botState = userDataCache.getUsersCurrentBotState(user_id);
        SendMessage replyMessage = null;

        switch (botState) {
            case NEW_POST: {
                switch (messageText) {
                    case "E'lon yaratish❗":
                    case "Создать обьявление❗":
                        userData.setLatestUserImagesPaths(null);
                        userDataCache.saveUserDataMap(user_id, userData);

                        replyMessage = getAdHeading(user_id);
                        userDataCache.setUsersCurrentBotState(user_id, BotState.GET_AD_TITLE);
                        /*replyMessage = getNewPost(user_id);*/
                        break;
                    default:
                        userDataCache.setUsersCurrentBotState(user_id, BotState.START);
                        replyMessage = startStateHandler.startingMessage(user_id);
                        break;
                }
                break;
            }
            case GET_AD_TITLE: {
                   /* case "image":
                        bot.sendPhoto(processImagePost(message));
                        break;*/
                userData.setTitle(messageText);
                userDataCache.saveUserDataMap(user_id, userData);
                replyMessage = getAdDescription(user_id);
                userDataCache.setUsersCurrentBotState(user_id, BotState.GET_DESCRIPTION);
                /*replyMessage = processTextPost(user_id, messageText);*/
                break;
            }
            case GET_DESCRIPTION:
                userData.setDescription(messageText);
                userDataCache.saveUserDataMap(user_id, userData);
                replyMessage = getPhoneNumber(user_id);
                userDataCache.setUsersCurrentBotState(user_id, BotState.GET_PHONE_NUMBER);
                break;
            case GET_PHONE_NUMBER:
                if (messageText.equals("contact")) {
                    Contact contact = message.getContact();
                    userData.setPhone_number(contact.getPhoneNumber());
                } else {
                    userData.setPhone_number(messageText);
                }
                userDataCache.saveUserDataMap(user_id, userData);
                replyMessage = askImages(user_id);
                userDataCache.setUsersCurrentBotState(user_id, BotState.GET_IMAGES);
                break;
            case GET_IMAGES:
                // for single photo
                if (messageText.equals("image")) {
                    bot.sendPhoto(processPhotos(message));
                    userDataCache.setUsersCurrentBotState(user_id, BotState.CONFIRM_AD);
                } else {
                    replyMessage = askImages(user_id);
                    if (userData.getLatestUserImage() == null) {
                        replyMessage = minimumOnePhoto(user_id);
                    } else {
                        if (messageText.equals("E'lonni joylash ✒")) {

                            userDataCache.setUsersCurrentBotState(user_id, BotState.CONFIRM_AD);
                        } else {
                            replyMessage = askImages(user_id);
                        }
                    }
                }
                break;
            case CONFIRM_AD:
                System.out.println("CONFIRM AD STATE NOW");
                break;
        }

        return replyMessage;
    }

    private SendMessage getAdHeading(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);

        userData.setLatestUserImage(null);
        userDataCache.saveUserDataMap(user_id, userData);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "ad_heading")));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton cancelButton = new KeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "cancel")));

        keyboardRow.add(cancelButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendMessage getAdDescription(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "description")));
        return replyMessage;
    }

    private SendMessage getPhoneNumber(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "ask_phone_number")));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton();
        keyboardButton1.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "send_phone_number")));
        keyboardButton1.setRequestContact(true);
        keyboardRow1.add(keyboardButton1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton cancelButton = new KeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "cancel")));
        keyboardRow2.add(cancelButton);

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendMessage askImages(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText(localeMessageService.getLocaleMessage(userData.getLang(), "ask_images"));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRowList = new ArrayList<>();

        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardButton publishButton = new KeyboardButton();
        publishButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "publish")));
        keyboardRow1.add(publishButton);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardButton cancelButton = new KeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "cancel")));
        keyboardRow2.add(cancelButton);

        keyboardRowList.add(keyboardRow1);
        keyboardRowList.add(keyboardRow2);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendPhoto processPhotos(Message message) {
        String user_id = message.getFrom().getId().toString();
        UserData userData = userDataCache.getUserDataMap(user_id);

        PhotoSize photo = message.getPhoto().get(message.getPhoto().size() - 1);
        GetFile getFile = new GetFile();
        getFile.setFileId(photo.getFileId());

        File file = bot.getFile(getFile);
        java.io.File imagePath = downloadImage(file, user_id, photo.getFileUniqueId());

        userData.setLatestUserImage(imagePath);
        userDataCache.saveUserDataMap(user_id, userData);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user_id);

        String text = "<b>" + userData.getTitle() + "</b>" + "\n\n" +
                "<i>:small_red_triangle_down: " + userData.getDescription() + "</i>" + "\n\n" +
                ":iphone: " + userData.getPhone_number() + " @" + message.getFrom().getUserName();
        sendPhoto.setCaption(EmojiParser.parseToUnicode(text));
        sendPhoto.setParseMode(ParseMode.HTML);

        userData.setText(text);
        userDataCache.saveUserDataMap(user_id, userData);

        InputFile inputFile = new InputFile();
        inputFile.setMedia(imagePath);
        sendPhoto.setPhoto(inputFile);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtonList = new ArrayList<>();

        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText(EmojiParser.parseToUnicode(":white_check_mark: Tasdiqlash"));
        confirmButton.setCallbackData("confirmChannelPost");

        inlineButtonList.add(confirmButton);
        inlineButtons.add(inlineButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);

        return sendPhoto;
    }

    private java.io.File downloadImage(File file, String user_id, String fileUniqueId) {
        try {
            URL url = new URL(file.getFileUrl(botToken));
            BufferedImage img = ImageIO.read(url);
            java.io.File imagePath = new java.io.File("src/main/resources/static/"+user_id+"_"+fileUniqueId+".jpg");
            ImageIO.write(img, "jpg", imagePath);
            return imagePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void forManyImages(Message message) {
        String user_id = message.getFrom().getId().toString();
        UserData userData = userDataCache.getUserDataMap(user_id);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);

        // get photo with best quality
        PhotoSize photo = message.getPhoto().get(message.getPhoto().size() - 1);
        GetFile getFile = new GetFile();
        getFile.setFileId(photo.getFileId());

        File file = bot.getFile(getFile);
        java.io.File imagePath = downloadImage(file, user_id, photo.getFileUniqueId());

        List<java.io.File> latestUserImagesPaths = userData.getLatestUserImagesPaths();
        if (latestUserImagesPaths == null) {
            latestUserImagesPaths = new ArrayList<>();
        }

        latestUserImagesPaths.add(imagePath);
        userData.setLatestUserImagesPaths(latestUserImagesPaths);
        userDataCache.saveUserDataMap(user_id, userData);
    }

    private SendMessage sendMultiplePhotos(String user_id, String username) {
        UserData userData = userDataCache.getUserDataMap(user_id);

/*
        List<InputMedia> inputMediaList = new ArrayList<>();
*/
        List<java.io.File> latestUserImagesPaths = userData.getLatestUserImagesPaths();

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId("-1001760318604");

        List<InputMedia> medias = new ArrayList<>();

        for (int i=0; i<latestUserImagesPaths.size(); i++) {
            java.io.File file = latestUserImagesPaths.get(i);
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto();
            inputMediaPhoto.setMedia(file, file.getName());

            if (i==0) {
                String text = "<b>" + userData.getTitle() + "</b>" + "\n\n" +
                        "<i>:small_red_triangle_down: " + userData.getDescription() + "</i>" + "\n\n" +
                        ":iphone: " + userData.getPhone_number() + " @" + username;

                inputMediaPhoto.setCaption(EmojiParser.parseToUnicode(text));
                inputMediaPhoto.setParseMode(ParseMode.HTML);
            }

            medias.add(inputMediaPhoto);
        }

        sendMediaGroup.setMedias(medias);
        bot.sendMediaGroup(sendMediaGroup);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setText("✅ Joylandi !");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton sendMessageButton = new KeyboardButton();
        sendMessageButton.setText(EmojiParser.parseToUnicode("E'lon yaratish:exclamation:"));

        keyboardRow.add(sendMessageButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendMessage minimumOnePhoto(String user_id) {
        UserData userData = userDataCache.getUserDataMap(user_id);
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText(EmojiParser.parseToUnicode(localeMessageService.getLocaleMessage(userData.getLang(), "first_send_images")));

        return replyMessage;
    }

   /* private SendPhoto confirmationMessage(String user_id, String username) {
        UserData userData = userDataCache.getUserDataMap(user_id);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user_id);
        sendPhoto.setParseMode(ParseMode.MARKDOWN);

        String text = "*" + userData.getTitle() + "*" + "\n\n" +
                "_:small_red_triangle_down: " + userData.getDescription() + "_" + "\n\n" +
                ":iphone: " + userData.getPhone_number() + " @" + username;
        sendPhoto.setCaption(text);



        InputFile inputFile = new InputFile();

        sendPhoto.setPhoto(inputFile);
    }*/

    private SendMessage getNewPost(String user_id) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setParseMode(ParseMode.MARKDOWN);
        replyMessage.setText("*Sizni keyingi yuboradigan xabaringiz kanalga joylanadi !*");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);

        List<KeyboardRow> keyboardRowList = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton cancelButton = new KeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode("Bekor qilish\uD83D\uDEAB"));

        keyboardRow.add(cancelButton);
        keyboardRowList.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
        replyMessage.setReplyMarkup(replyKeyboardMarkup);

        return replyMessage;
    }

    private SendMessage processTextPost(String user_id, String text) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(user_id);
        replyMessage.setText(text);

        UserData userData = userDataCache.getUserDataMap(user_id);
        userData.setText(text);
        userDataCache.saveUserDataMap(user_id,userData);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtonList = new ArrayList<>();

        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText(EmojiParser.parseToUnicode(":white_check_mark: Tasdiqlash"));
        confirmButton.setCallbackData("confirmChannelPost");

        inlineButtonList.add(confirmButton);
        inlineButtons.add(inlineButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        replyMessage.setReplyMarkup(inlineKeyboardMarkup);
        return replyMessage;
    }



/*    private SendPhoto processImagePost(Message message) {
        String user_id = message.getFrom().getId().toString();
        String text = message.getCaption();

        UserData userData = userDataCache.getUserDataMap(user_id);
        userData.setText(text);
        userDataCache.saveUserDataMap(user_id, userData);

        *//*ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(user_id);
        forwardMessage.setMessageId(message.getMessageId());
        *//*

        GetFile getFile = new GetFile();
        getFile.setFileId(message.getPhoto().get(2).getFileId());

        File file = bot.getFile(getFile);

        java.io.File imagePath = downloadImage(file, user_id);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(user_id);
        sendPhoto.setCaption(text);

        InputFile inputFile = new InputFile();
        inputFile.setMedia(imagePath);
        sendPhoto.setPhoto(inputFile);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> inlineButtonList = new ArrayList<>();

        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText(EmojiParser.parseToUnicode(":white_check_mark: Tasdiqlash"));
        confirmButton.setCallbackData("confirmChannelPost");

        inlineButtonList.add(confirmButton);
        inlineButtons.add(inlineButtonList);
        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);

        return sendPhoto;
    }*/


}
