package com.example.eventerapp.entity;

import java.util.List;
import java.util.Map;

public class Event {

    private Long roomId;

    private String description;

    private Map<Long, Boolean> members;

    private String title;

    private Long myId;

    private String floorId;

    private String contactPhone;

    private String startDate;

    private String ownerEmail;

    public Event() {}

    public Event(String Long, String description, Map<Long, Boolean> members, String title, String floorId, String contactPhone, String startDate) {
        this.roomId = roomId;
        this.description = description;
        this.members = members;
        this.title = title;
        this.floorId = floorId;
        this.contactPhone = contactPhone;
        this.startDate = startDate;
    }

    public Long getRoomId() {
        return roomId;
    }

    public Long getMyId() {
        return myId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setMyId(Long myId) {
        this.myId = myId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<Long, Boolean> getMembers() {
        return members;
    }

    public void setMembers(Map<Long, Boolean> members) {
        this.members = members;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
