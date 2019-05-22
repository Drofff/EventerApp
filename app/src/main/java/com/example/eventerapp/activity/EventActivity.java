package com.example.eventerapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventerapp.viewModel.PhotosViewModel;
import com.example.eventerapp.R;
import com.example.eventerapp.entity.Event;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    Event currentEvent;

    ImageView eventerPhoto;

    TextView title;

    TextView membersCount;

    TextView description;

    TextView startDate;

    Button findOnMap;

    TextView detailedRoomNumber;

    PhotosViewModel photosViewModel;

    FloatingActionButton actionButton;

    FloatingActionButton callToOwner;

    String eventName;

    ImageButton backHomeButton;

    public static String callNumber;

    public static final int REQUEST_CODE_CALL = 55;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        actionButton = findViewById(R.id.floatingActionButton);

        photosViewModel = HomePage.photosViewModel;

        backHomeButton = findViewById(R.id.backHomeFromEvent);

        eventerPhoto = findViewById(R.id.eventerDetailedPhoto);

        title = findViewById(R.id.eventerDetailedTitle);

        membersCount = findViewById(R.id.detailedEventerMembers);

        description = findViewById(R.id.detailedDescription);

        startDate = findViewById(R.id.startDateDetailed);

        findOnMap = findViewById(R.id.findOnMapDetailed);

        detailedRoomNumber = findViewById(R.id.detaiiledRoomNumber);

        callToOwner = findViewById(R.id.callToEventOwner);

        Intent intent = getIntent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        final String idOfEvent = intent.getStringExtra("id");
        if (idOfEvent == null) {
            NavUtils.navigateUpFromSameTask(this);
        }


        if (PhotosViewModel.idToQuery.containsKey(idOfEvent)) {
            eventerPhoto.setImageBitmap(photosViewModel.getPhoto(PhotosViewModel.ROOM_PHOTO, PhotosViewModel.idToQuery.get(idOfEvent), eventerPhoto.getWidth(), eventerPhoto.getHeight()));
        }

        backHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(EventActivity.this);
            }
        });

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
                eventName = event.getTitle();
                event.setStartDate(dataSnapshot.child("startDate").getValue(String.class));
                event.setRoomId(dataSnapshot.child("roomId").getValue(Long.class));

                Map<String, Boolean> dataOfUser = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.child("members").getChildren()) {
                    dataOfUser.put(snapshot.getKey(), true);
                }
                event.setMembers(dataOfUser);
                currentEvent = event;

                title.setText(event.getTitle());
                description.setText(event.getDescription());
                membersCount.setText(event.getMembers().size() + "");

                final String phoneNumber = event.getContactPhone();

                callToOwner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(EventActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber.trim()));
                            startActivity(callIntent);
                        } else {
                            String [] perms = {Manifest.permission.CALL_PHONE};
                            callNumber = phoneNumber.trim();
                            ActivityCompat.requestPermissions(EventActivity.this, perms, REQUEST_CODE_CALL);
                        }
                    }
                });

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
                        if (PhotosViewModel.idToQuery.containsValue(uri) == false) {
                            Glide.with(EventActivity.this).load(uri).apply(new RequestOptions().override(eventerPhoto.getWidth(), eventerPhoto.getHeight())).centerCrop().into(eventerPhoto);
                            photosViewModel.getPhoto(PhotosViewModel.ROOM_PHOTO, uri.toString(), eventerPhoto.getWidth(), eventerPhoto.getHeight());
                            PhotosViewModel.idToQuery.put(idOfEvent, uri.toString());
                            System.out.println("Cache not used");
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(EventActivity.this, ChatActivity.class);
                chatIntent.putExtra("id", idOfEvent);
                chatIntent.putExtra("name", eventName);
                startActivity(chatIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CALL && callNumber != null) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callNumber));
            startActivity(callIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
