package com.example.firebasemessenger.models;

import java.util.Date;

public class ChatMessage {
    private String text;
    private String senderNick, senderKey;
    private long time;
    private boolean deletedForSender;
    private String key;

    public ChatMessage(String text, String senderNick, String senderKey) {
        this.text = text;
        this.senderNick = senderNick;
        this.senderKey = senderKey;

        time = new Date().getTime();
        deletedForSender = false;
    }

    public ChatMessage(){

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSenderNick() {
        return senderNick;
    }

    public void setSenderNick(String senderNick) {
        this.senderNick = senderNick;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public boolean isDeletedForSender() {
        return deletedForSender;
    }

    public void setDeletedForSender(boolean deletedForSender) {
        this.deletedForSender = deletedForSender;
    }
}
