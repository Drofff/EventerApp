package com.example.eventerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventerapp.entity.Building;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingsAdapter extends RecyclerView.Adapter<BuildingsAdapter.BuildingView> {

    List<Building> buildings = new ArrayList<>();
    Context contextHome;
    Map<String, String> dbKeys;
    PhotosViewModel photosViewModel;
    public static boolean firstTime = true;

    public BuildingsAdapter(Context contextHome, List<Building> buildings, Map<String, String> dbKeys, PhotosViewModel photosViewModel) {
        this.contextHome = contextHome;
        this.dbKeys = dbKeys;
        this.buildings = buildings;
        this.photosViewModel = photosViewModel;
    }

    @NonNull
    @Override
    public BuildingView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new BuildingView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BuildingView buildingView, int i) {
            Building building = buildings.get(i);
            buildingView.setAddress(building.address, contextHome);

            System.out.println(building.floors.entrySet() + " ------------- I AM HERE");

            buildingView.setOpenInApp(dbKeys.get(building.emailOfUser),  building.floors.size(), contextHome);

            String photoNameInMemory = "buildingBY" + building.emailOfUser;

            Bitmap cachedPhoto = photosViewModel.getPhoto(PhotosViewModel.BUILDING_PHOTO, photoNameInMemory, buildingView.getImageView().getWidth(), buildingView.getImageView().getHeight());

            if (cachedPhoto != null) {
                buildingView.getImageView().setImageBitmap(cachedPhoto);
            } else {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference reference = storage.getReference("images");
                reference.child(photoNameInMemory).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(contextHome).load(uri).into(buildingView.getImageView());
                    }
                });
            }
    }


    @Override
    public int getItemCount() {
        return buildings.size();
    }

    public void changeData(List<Building> buildings) {
        this.buildings = buildings;
        notifyDataSetChanged();
    }

    public static class BuildingView extends RecyclerView.ViewHolder {

        ImageView buildingPhoto;

        TextView address;

        Button findOnMap;

        LinearLayout openInApp;


        public BuildingView(@NonNull View itemView) {
            super(itemView);
            buildingPhoto = (ImageView) itemView.findViewById(R.id.imageView);
            address = (TextView) itemView.findViewById(R.id.textView5);
            findOnMap = (Button) itemView.findViewById(R.id.button3);
            openInApp = (LinearLayout) itemView.findViewById(R.id.constL);
        }


        public void setAddress(final String address, final Context context) {
            this.address.setText(address);
            findOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = new Uri.Builder().scheme("geo")
                                .query("0,0")
                                .appendQueryParameter("q", address)
                                .build();
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            });
        }

        public ImageView getImageView() {
            return this.buildingPhoto;
        }


        public void setOpenInApp(final String buildingId, final int countOfFloors, final Context context) {
            this.openInApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RoomActivity.class);
                    intent.putExtra("id", buildingId);
                    intent.putExtra("floors", countOfFloors);
                    context.startActivity(intent);
                }
            });
        }

    }
}
