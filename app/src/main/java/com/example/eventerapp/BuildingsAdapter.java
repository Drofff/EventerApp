package com.example.eventerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventerapp.entity.Building;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingsAdapter extends RecyclerView.Adapter<BuildingsAdapter.BuildingView> {

    List<Building> buildings = new ArrayList<>();
    Context contextHome;
    Map<String, Bitmap> photos;

    public BuildingsAdapter(List<Building> buildings, Context contextHome, Map<String, Bitmap> photos) {
        this.buildings = buildings;
        this.contextHome = contextHome;
        this.photos = photos;
    }

    @NonNull
    @Override
    public BuildingView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new BuildingView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingView buildingView, int i) {
        Building building = buildings.get(i);
        buildingView.setAddress(building.address, contextHome);
        buildingView.setPhoto(photos.get(building.emailOfUser));
    }

    @Override
    public int getItemCount() {
        return buildings.size();
    }

    public void changeBuildingsList(List<Building> buildings, Map<String, Bitmap> photos) {
        this.buildings = buildings;
        this.photos = photos;
        notifyDataSetChanged();
    }

    public static class BuildingView extends RecyclerView.ViewHolder {

        ImageView buildingPhoto;

        TextView address;

        Button findOnMap;

        Button openInApp;


        public BuildingView(@NonNull View itemView) {
            super(itemView);
            buildingPhoto = (ImageView) itemView.findViewById(R.id.imageView);
            address = (TextView) itemView.findViewById(R.id.textView5);
            findOnMap = (Button) itemView.findViewById(R.id.button3);
            openInApp = (Button) itemView.findViewById(R.id.button4);
        }

        public void setPhoto(Bitmap bitmap) {
            buildingPhoto.setImageBitmap(bitmap);
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

        public void setOpenInApp() {
            //later i will do it
        }

    }
}
