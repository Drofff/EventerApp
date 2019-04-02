package com.example.eventerapp.entity;

public class Room {

    private int roomId;

    private String floorId;

    private String ownerId;

    private int roomNumber;

    public Room() {}

    public Room(int roomId, String floorId, String ownerId, int roomNumber) {
        this.roomId = roomId;
        this.floorId = floorId;
        this.ownerId = ownerId;
        this.roomNumber = roomNumber;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}
