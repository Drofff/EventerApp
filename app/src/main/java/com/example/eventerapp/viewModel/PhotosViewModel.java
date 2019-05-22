package com.example.eventerapp.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class PhotosViewModel extends AndroidViewModel {

    static Map<String, Bitmap> photosCache;

    private static StorageReference userPhotosRef;

    private static StorageReference buildingsPhotosRef;

    private static StorageReference roomPhotosRef;

    public static final int USER_PHOTO = 1;

    public static final int BUILDING_PHOTO = 2;

    public static final int ROOM_PHOTO = 3;

    public static Map<String, String> idToQuery = new HashMap<>();

    private Context context;

    public PhotosViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        userPhotosRef = storage.getReference().child("users");
        buildingsPhotosRef = storage.getReference().child("images");
        roomPhotosRef = storage.getReference().child("rooms");
        photosCache = new HashMap<>();
    }

    public Bitmap getPhoto(final int photoType, final String query, final int w, final int h) {

        System.out.println(photosCache.keySet() + " " + query + " for this photo");
        if (photosCache.containsKey(query)) {
            return photosCache.get(query);
        }

        if (photoType == BUILDING_PHOTO) {
            buildingsPhotosRef.child(query).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap photo = Picasso.with(context).load(uri).get();
                                PhotosViewModel.photosCache.put(query, photo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } else {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        PhotosViewModel.photosCache.put(query, Picasso.with(context).load(query).resize(w, h).centerCrop().get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return null;

    }


}
