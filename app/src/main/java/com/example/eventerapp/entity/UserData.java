package com.example.eventerapp.entity;

public class UserData {

    private String email;

    private String roomId;

    private String currentPostion;

    public UserData() {}

    public UserData(String email, String roomId, String currentPostion) {
        this.email = email;
        this.roomId = roomId;
        this.currentPostion = currentPostion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getCurrentPostion() {
        return currentPostion;
    }

    public void setCurrentPostion(String currentPostion) {
        this.currentPostion = currentPostion;
    }
}
