package com.example.eventerapp.entity;

import java.util.List;

public class Floor {

    private String idOfBuilding;

    private List<Room> rooms;

    public Floor(String idOfBuilding, List<Room> rooms) {
        this.idOfBuilding = idOfBuilding;
        this.rooms = rooms;
    }

    public Floor() {}

    public String getIdOfBuilding() {
        return idOfBuilding;
    }

    public void setIdOfBuilding(String idOfBuilding) {
        this.idOfBuilding = idOfBuilding;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
