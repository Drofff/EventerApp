package com.example.eventerapp;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.example.eventerapp.entity.Building;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    LiveData<List<Building>> buildingList;

    LiveData<Map<String, Bitmap>> photos;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("buildings");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                photos.getValue().clear();
                List<Building> changedList = new ArrayList<>();
                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                    Building building = postSnap.getValue(Building.class);
                    changedList.add(building);
                    refreshPhoto(building.emailOfUser);
                }
                buildingList.getValue().clear();
                buildingList.getValue().addAll(changedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public LiveData<List<Building>> getBuildingList() {
        return buildingList;
    }

    public LiveData<Map<String, Bitmap>> getPhotos() {
        return photos;
    }

    private void refreshPhoto(final String email) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("images");
        int maxSize = 4000 * 4000;
        reference.getStream().addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                photos.getValue().put(email, BitmapFactory.decodeStream(taskSnapshot.getStream()));
            }
        });
    }
}
