package com.example.eventerapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.eventerapp.entity.Event;
import com.example.eventerapp.entity.Floor;
import com.example.eventerapp.entity.Room;
import com.example.eventerapp.entity.UserData;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    private String id;

    private int countOfFloors;

    private static final int KEY_FOR_LOADER = 34;

    RecyclerView recyclerView;

    DatabaseReference firebaseDatabase;

    ValueEventListener valueEventListener;

    ProgressBar progressBar;

    List<Event> eventsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        recyclerView = (RecyclerView) findViewById(R.id.roomShow);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new RoomsAdapter(this, new ArrayList<Event>()));
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getStringExtra("id");
        countOfFloors = getIntent().getIntExtra("floors", 0);
        if (id == null || id.equals("") || countOfFloors < 1) {
            Toast.makeText(this, "Building do not exists or empty", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, HomePage.class));
        }


    }

    private void loadData() {

        firebaseDatabase = FirebaseDatabase.getInstance().getReference(DatabaseContract.EVENTS_KEY);

        final List<String> floorsIds = new LinkedList<>();
        for (int i = 1; i <= countOfFloors; i++) {
            floorsIds.add(i + ":" + id);
        }


        valueEventListener = firebaseDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);

                eventsList.clear();

                for (DataSnapshot s : dataSnapshot.getChildren()) {

                    if (s.child("floorId").getValue(String.class).equals(floorsIds.get(0))) {
                        Event event = new Event();
                        event.setFloorId(s.child("floorId").getValue(String.class));
                        event.setContactPhone(s.child("contactPhone").getValue(String.class));
                        event.setDescription(s.child("description").getValue(String.class));
                        event.setMyId(s.child("myId").getValue(Long.class));
                        event.setOwnerEmail(s.child("ownerEmail").getValue(String.class));
                        event.setTitle(s.child("title").getValue(String.class));
                        event.setStartDate(s.child("startDate").getValue(String.class));
                        event.setRoomId(s.child("roomId").getValue(Long.class));

                        Map<Long, Boolean> dataOfUser = new HashMap<>();

                        for (DataSnapshot snapshot : s.child("members").getChildren()) {
                            dataOfUser.put(Long.parseLong(snapshot.getKey()), true);
                        }
                        event.setMembers(dataOfUser);
                        eventsList.add(event);
                    }

                }


                eventsList.sort(new Comparator<Event>() {
                    @Override
                    public int compare(Event o1, Event o2) {
                        return o1.getMembers().size() - o2.getMembers().size();
                    }
                });


                if (eventsList != null && eventsList.size() > 0) {
                    recyclerView.setAdapter(new RoomsAdapter(RoomActivity.this, eventsList));
                }

                progressBar.setVisibility(View.INVISIBLE);

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

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseDatabase.removeEventListener(valueEventListener);
    }
}
