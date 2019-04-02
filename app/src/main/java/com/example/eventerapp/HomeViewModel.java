package com.example.eventerapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.example.eventerapp.utils.DatabaseContract;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeViewModel extends AndroidViewModel {

    DatabaseReference database;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        database = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDINGS_KEY);
    }

    public DatabaseReference getDatabase() {
        return database;
    }


}
