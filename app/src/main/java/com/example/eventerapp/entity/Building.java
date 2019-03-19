package com.example.eventerapp.entity;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Building {

    public String address;

    public String photoUrl;

    public Map<String, Boolean> floors;

    public String emailOfUser;

    public Building() {}

    public Building(String address, String photoUrl, Map<String, Boolean> floors, String emailOfUser) {
        this.address = address;
        this.photoUrl = photoUrl;
        this.floors = floors;
        this.emailOfUser = emailOfUser;
    }
}
