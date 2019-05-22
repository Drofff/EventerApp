package com.example.eventerapp.activity;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventerapp.adapter.BuildingsAdapter;
import com.example.eventerapp.viewModel.HomeViewModel;
import com.example.eventerapp.viewModel.PhotosViewModel;
import com.example.eventerapp.R;
import com.example.eventerapp.entity.Building;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView nameView;

    ImageView userPhoto;

    static Bitmap photo;

    private FirebaseUser user;

    public static Map<String, String> openUris = new HashMap<>();

    RecyclerView recyclerView;

    ValueEventListener valueEventListener;

    BuildingsAdapter adapter;

    ProgressBar progressBar;

    LinearLayout buildingParent;

    NavigationView navigationView;

    public static final int KEY_FOR_LOADER = 34;

    private boolean canAddNewBuilding = true;

    private boolean hasRoom = false;

    ImageView notFoundImage;

    TextView notFoundText;

    public static PhotosViewModel photosViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Eventer");

        roomCheck();

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        notFoundImage = findViewById(R.id.notFoundBuild);
        notFoundText = findViewById(R.id.notFoundBuildText);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        linearLayoutManager.setReverseLayout(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new BuildingsAdapter(this, new ArrayList<Building>(), Collections.<String, String>emptyMap(), photosViewModel));

        final SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                View currentView = snapHelper.findSnapView(linearLayoutManager);
                int pos = linearLayoutManager.getPosition(currentView);

                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(pos);
                buildingParent = viewHolder.itemView.findViewById(R.id.cardForBuilding);
                buildingParent.animate().scaleX(1).scaleY(1).setDuration(550).setInterpolator(new AccelerateInterpolator()).start();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                     buildingParent.animate().scaleX(1).scaleY(1).setDuration(550).setInterpolator(new AccelerateInterpolator()).start();
                } else {
                     buildingParent.animate().scaleY(0.7f).scaleX(0.7f).setDuration(350).setInterpolator(new AccelerateInterpolator()).start();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(HomePage.this, CameraActivity.class);
               startActivity(intent);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View view = navigationView.getHeaderView(0);

        userPhoto = (ImageView) view.findViewById(R.id.userPicture);
        nameView = (TextView) view.findViewById(R.id.nameUser);

        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        valueEventListener = homeViewModel.getDatabase().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter = new BuildingsAdapter(HomePage.this, updateData(dataSnapshot), openUris, photosViewModel);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private List<Building> updateData(final DataSnapshot dataSnapshot) {
        final GenericTypeIndicator<Map<String, Object>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Object>>() {};

        List<Building> buildings = new LinkedList<>();

        for (DataSnapshot postSnap : dataSnapshot.getChildren()) {

            Map<String, Object> result = postSnap.getValue(genericTypeIndicator);
            final String email = (String) result.get("emailOfUser");
            openUris.put(email, postSnap.getKey());

            try {

                if (email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    canAddNewBuilding = false;
                    MenuItem item = navigationView.getMenu().findItem(R.id.nav_gallery);
                    item.setTitle(Html.fromHtml("<font color='gray'>" + item.getTitle() + "</font>"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, Boolean> floorsNames = new HashMap<>();
            DataSnapshot floorsSnap = postSnap.child("floors");
            for (DataSnapshot s : floorsSnap.getChildren()) {
                floorsNames.put(s.getKey(), true);
            }

            buildings.add(new Building((String) result.get("address"), (String) result.get("photoUrl"), floorsNames, email));
        }

        if (buildings.size() < 1) {
            notFoundImage.setVisibility(View.VISIBLE);
            notFoundText.setVisibility(View.VISIBLE);
        }

        return buildings;


    }

    @Override
    protected void onStart() {
        super.onStart();
        fillUserData();
    }

    private void roomCheck() {
        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChild("roomId")) {
                       hasRoom = true;
                       MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_camera);
                       menuItem.setTitle(Html.fromHtml("<font color='gray'>" + menuItem.getTitle() + "</font>"));
                       break;
                    } else {
                        hasRoom = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fillUserData() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {

            nameView.setText(user.getDisplayName());

            try {
                final FirebaseUser forThreadCopy = user;
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... voids) {
                        try {
                            return BitmapFactory.decodeStream(new URL(forThreadCopy.getPhotoUrl().toString()).openConnection().getInputStream());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap o) {
                        super.onPostExecute(o);
                        photo = o;
                        if (photo != null) {
                            userPhoto.setImageBitmap(photo);
                        }
                    }
                }.execute();


            } catch (Exception e) {
                Toast.makeText(this, "Can't load image", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_page, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent intent = new Intent(HomePage.this, SearchingResult.class);
                intent.putExtra(SearchManager.QUERY, s);
                intent.setAction(Intent.ACTION_SEARCH);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            if (hasRoom) {
                Toast.makeText(HomePage.this, "You already have a room", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(HomePage.this, RoomAdd.class);
                startActivity(intent);
            }

        } else if (id == R.id.nav_gallery && canAddNewBuilding == true) {
            Intent intent = new Intent(this, AddBuilding.class);
            startActivity(intent);

        } else if (id == R.id.nav_gallery && canAddNewBuilding == false) {
            Toast.makeText(this, "You can't add more than 1 building", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_send) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.my_qr_nav) {
            if (hasRoom) {
                Intent intent = new Intent(this, MyQrActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(HomePage.this, "You do not have a room", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.my_status_nav) {
            Intent in = new Intent(this, MyStatusActivity.class);
            startActivity(in);
        } else if (id == R.id.my_chats_nav) {
            Intent in = new Intent(this, MyChatsActivity.class);
            startActivity(in);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewModelProviders.of(this).get(HomeViewModel.class).getDatabase().removeEventListener(valueEventListener);
    }
}
