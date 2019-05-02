package com.example.eventerapp.entity;

public class Message {

    private Long id;

    private String author;

    private String messageText;

    private String photoUrl;

    public Message() {}

    public Message(Long id, String author, String messageText, String photoUrl) {
        this.author = author;
        this.id = id;
        this.messageText = messageText;
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
