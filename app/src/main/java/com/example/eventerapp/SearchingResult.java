package com.example.eventerapp;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventerapp.entity.Building;
import com.example.eventerapp.utils.DatabaseContract;
import com.example.eventerapp.utils.DatabaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchingResult extends AppCompatActivity {

    String query;

    RecyclerView recyclerView;

    ImageView notFoundImage;

    TextView sorryText;

    PhotosViewModel photosViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_result);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        photosViewModel = ViewModelProviders.of(this).get(PhotosViewModel.class);
        Intent inputIntent = getIntent();
        query = inputIntent.getStringExtra(SearchManager.QUERY);
        if (query != null) {
            actionBar.setTitle("Result for " + (query.length() > 10 ? query.substring(0, 10) + ".." : query));
        }
        if (inputIntent.getAction().equals(Intent.ACTION_SEARCH) && query.length() > 0) {
            recyclerView = (RecyclerView) findViewById(R.id.search_recyler_view);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            notFoundImage = findViewById(R.id.photoSorry);
            sorryText = findViewById(R.id.sorryText);
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration decoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(decoration);
            recyclerView.setAdapter(new BuildingsAdapter(this, new ArrayList<Building>(), Collections.<String, String>emptyMap(), photosViewModel));
            recyclerView.setHasFixedSize(true);
        } else {
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        }
        doSearch();
    }

    public int searchingMatch(String query, String template) {
        query = query.toLowerCase();
        template = template.toLowerCase();
        String [] queryParts = query.split(" ");
        int matches = 0;
        for (String s : queryParts) {

            if (template.matches(".*" + s + ".*")) {

                matches += 2;

            } else {

                int letterCounter;
                String[] templateParts = template.split(" ");

                for (String partOfT : templateParts) {

                    letterCounter = 0;

                    for (char c : s.toCharArray()) {

                        if (partOfT.matches(".*" + c + ".*")) ++letterCounter;

                    }

                    if (letterCounter >= (partOfT.length() / 3) * 2) {

                        ++matches;

                    }

                }

            }

        }
        return matches;
    }

    public void doSearch() {
        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDINGS_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> dbKeys = new LinkedHashMap<>();
                List<Building> buildings = new LinkedList<>();
                final Map<Building, Integer> searchingResults = new HashMap<>();
                for (DataSnapshot s : dataSnapshot.getChildren()) {

                    int searchingResult = searchingMatch(s.child("address").getValue(String.class), query);
                    if (searchingResult > 0) {
                        Building build = new Building();
                        build.emailOfUser = s.child("emailOfUser").getValue(String.class);
                        build.photoUrl = DatabaseUtils.makeBuildingPhotoUrl(build.emailOfUser);
                        build.address = s.child("address").getValue(String.class);

                        Map<String, Boolean> floorsMap = new HashMap<>();

                        for (DataSnapshot snap : s.child("floors").getChildren()) {
                            floorsMap.put(snap.getKey(), true);
                        }

                        build.floors = floorsMap;

                        dbKeys.put(build.emailOfUser, s.getKey());
                        searchingResults.put(build, searchingResult);
                        buildings.add(build);
                    }
                }
                buildings.sort(new Comparator<Building>() {
                    @Override
                    public int compare(Building o1, Building o2) {
                        return searchingResults.get(o1) - searchingResults.get(o2);
                    }
                });

                if (buildings.size() < 1) {
                    sorryText.setVisibility(View.VISIBLE);
                    notFoundImage.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setAdapter(new BuildingsAdapter(SearchingResult.this, buildings, dbKeys, photosViewModel));
                }
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
}
