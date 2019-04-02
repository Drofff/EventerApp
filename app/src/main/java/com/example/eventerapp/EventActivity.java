package com.example.eventerapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventerapp.entity.Event;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    Event currentEvent;

    ImageView eventerPhoto;

    TextView title;

    TextView membersCount;

    TextView description;

    TextView phoneNumber;

    TextView startDate;

    Button findOnMap;

    TextView detailedRoomNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        eventerPhoto = findViewById(R.id.eventerDetailedPhoto);

        title = findViewById(R.id.eventerDetailedTitle);

        membersCount = findViewById(R.id.detailedEventerMembers);

        description = findViewById(R.id.detailedDescription);

        phoneNumber = findViewById(R.id.phoneNumberDetailed);

        startDate = findViewById(R.id.startDateDetailed);

        findOnMap = findViewById(R.id.findOnMapDetailed);

        detailedRoomNumber = findViewById(R.id.detaiiledRoomNumber);

        Intent intent = getIntent();

        String idOfEvent = intent.getStringExtra("id");
        if (idOfEvent == null) {
            NavUtils.navigateUpFromSameTask(this);
        }
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).child(idOfEvent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Event event = new Event();
                event.setFloorId(dataSnapshot.child("floorId").getValue(String.class));
                event.setContactPhone(dataSnapshot.child("contactPhone").getValue(String.class));
                event.setDescription(dataSnapshot.child("description").getValue(String.class));
                event.setMyId(dataSnapshot.child("myId").getValue(Long.class));
                event.setOwnerEmail(dataSnapshot.child("ownerEmail").getValue(String.class));
                event.setTitle(dataSnapshot.child("title").getValue(String.class));
                event.setStartDate(dataSnapshot.child("startDate").getValue(String.class));
                event.setRoomId(dataSnapshot.child("roomId").getValue(Long.class));

                Map<Long, Boolean> dataOfUser = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                    dataOfUser.put(Long.parseLong(snapshot.getKey()), true);
                }
                event.setMembers(dataOfUser);
                currentEvent = event;

                title.setText(event.getTitle());
                description.setText(event.getDescription());
                membersCount.setText(event.getMembers().size() + "");
                phoneNumber.setText(event.getContactPhone());
                startDate.setText(event.getStartDate());

                FirebaseDatabase.getInstance().getReference().child(DatabaseContract.FLOORS_KEY).child(event.getFloorId()).child("rooms").child(event.getRoomId() + "").child("roomNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long roomNum = dataSnapshot.getValue(Long.class);
                        if (roomNum != null) {
                            detailedRoomNumber.setText(roomNum + " at " + event.getFloorId().split(":")[0] + " floor");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String address[] = event.getFloorId().split(":");
                if (address[1] != null) {
                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDINGS_KEY).child(address[1]).child("address").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final String point = dataSnapshot.getValue(String.class);
                            if (point != null) {
                                findOnMap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent googleMapIntent = new Intent(Intent.ACTION_VIEW);
                                        Uri.Builder builder = new Uri.Builder()
                                                .scheme("geo")
                                                .query("0,0")
                                                .appendQueryParameter("q", point);
                                        googleMapIntent.setData(builder.build());
                                        if (googleMapIntent.resolveActivity(getPackageManager()) != null) {
                                            startActivity(googleMapIntent);
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                FirebaseStorage.getInstance().getReference().child("rooms").child(event.getOwnerEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(EventActivity.this).load(uri).resize(eventerPhoto.getWidth(), eventerPhoto.getHeight()).centerCrop().into(eventerPhoto);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
    //TODO find on map button for detailed page
}
