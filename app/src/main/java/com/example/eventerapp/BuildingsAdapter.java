package com.example.eventerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventerapp.entity.Building;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingsAdapter extends RecyclerView.Adapter<BuildingsAdapter.BuildingView> {

    List<Building> buildings = new ArrayList<>();
    Context contextHome;
    Map<String, String> dbKeys;

    public BuildingsAdapter(Context contextHome, List<Building> buildings, Map<String, String> dbKeys) {
        this.contextHome = contextHome;
        this.dbKeys = dbKeys;
        this.buildings = buildings;
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

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference reference = storage.getReference("images");
            reference.child("buildingBY" + building.emailOfUser).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(contextHome).load(uri).into(buildingView.getImageView());
                }
            });
    }
        //reload rooms on data change

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

        ImageView star1;

        ImageView star2;

        ImageView star3;

        ImageView star4;

        ImageView star5;

        ImageButton findOnMap;

        ConstraintLayout openInApp;


        public BuildingView(@NonNull View itemView) {
            super(itemView);
            buildingPhoto = (ImageView) itemView.findViewById(R.id.imageView);
            address = (TextView) itemView.findViewById(R.id.textView5);
            findOnMap = (ImageButton) itemView.findViewById(R.id.button3);
            openInApp = (ConstraintLayout) itemView.findViewById(R.id.constL);
            star1 = (ImageView) itemView.findViewById(R.id.star1);
            star2 = (ImageView) itemView.findViewById(R.id.star2);
            star3 = (ImageView) itemView.findViewById(R.id.star3);
            star4 = (ImageView) itemView.findViewById(R.id.star4);
            star5 = (ImageView) itemView.findViewById(R.id.star5);
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
            FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDING_RATE_KEY).child(buildingId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Long> rates = new ArrayList<>();
                    long sum = 0;
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        Long num = s.getValue(Long.class);
                        rates.add(num);
                        sum += num;
                    }
                    long finalRate = rates.size() < 1 ? 0 : sum / rates.size();

                    if (finalRate == 1) {
                            star1.setVisibility(View.VISIBLE);
                    } else if (finalRate == 2) {
                        star1.setVisibility(View.VISIBLE);
                        star2.setVisibility(View.VISIBLE);
                    } else if (finalRate == 3) {
                        star1.setVisibility(View.VISIBLE);
                        star2.setVisibility(View.VISIBLE);
                        star3.setVisibility(View.VISIBLE);
                    } else if (finalRate == 4) {
                        star1.setVisibility(View.VISIBLE);
                        star2.setVisibility(View.VISIBLE);
                        star3.setVisibility(View.VISIBLE);
                        star4.setVisibility(View.VISIBLE);
                    } else if (finalRate == 5) {
                        star1.setVisibility(View.VISIBLE);
                        star2.setVisibility(View.VISIBLE);
                        star3.setVisibility(View.VISIBLE);
                        star4.setVisibility(View.VISIBLE);
                        star5.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
