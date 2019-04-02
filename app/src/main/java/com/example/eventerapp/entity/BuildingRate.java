package com.example.eventerapp.entity;

public class BuildingRate {

    private String buildingId;

    private byte rate;

    public BuildingRate() {}

    public BuildingRate(String buildingId, byte rate) {
        this.buildingId = buildingId;
        this.rate = rate;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public byte getRate() {
        return rate;
    }

    public void setRate(byte rate) {
        this.rate = rate;
    }
}
