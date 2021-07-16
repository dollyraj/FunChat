package com.example.funchat.ModelClass;

public class Messages {

    String message;
    String senderId;
    String receiverId;
    String type;
    String url;
    String currAddress;
   String date;
   String time;

    public Messages() {
    }

    public Messages(String message, String senderId, String receiverId, String type, String url, String currAddress, String date, String time) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.url = url;
        this.currAddress = currAddress;
        this.date = date;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCurrAddress() {
        return currAddress;
    }

    public void setCurrAddress(String currAddress) {
        this.currAddress = currAddress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
