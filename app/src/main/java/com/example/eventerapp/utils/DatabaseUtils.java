package com.example.eventerapp.utils;

import com.example.eventerapp.entity.Floor;

import java.util.LinkedList;
import java.util.List;

public class DatabaseUtils {

    public static String makeBuildingPhotoUrl(String email) {
        return "buildingBY" + email;
    }
}
