package com.azizbek.channelpostingbotheroku.cache;

import java.io.File;
import java.util.List;

/**
 * @author Azizbek Avazov
 * @created 15/04/2022
 */

public class UserData {
    private String lang;
    private String text;
    private boolean isImage;
    private String title;
    private String description;
    private String phone_number;
    private File latestUserImage;

    private List<File> latestUserImagesPaths;

    public List<File> getLatestUserImagesPaths() {
        return latestUserImagesPaths;
    }

    public void setLatestUserImagesPaths(List<File> latestUserImagesPaths) {
        this.latestUserImagesPaths = latestUserImagesPaths;
    }

    public File getLatestUserImage() {
        return latestUserImage;
    }

    public void setLatestUserImage(File latestUserImage) {
        this.latestUserImage = latestUserImage;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
