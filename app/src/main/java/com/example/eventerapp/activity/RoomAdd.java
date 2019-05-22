package com.example.eventerapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventerapp.R;
import com.example.eventerapp.entity.Room;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class RoomAdd extends AppCompatActivity {

    Spinner chooseBuild;

    ArrayAdapter<String> arrayAdapter;

    Spinner chooseFloor;

    EditText roomNumber;

    FloatingActionButton choosePhoto;

    Room objRoom = new Room();

    ImageView photoToRoom;

    ArrayAdapter<String> adapterForFloors;

    CheckBox checkBox;

    Button addButton;

    //collected data
    String buildingSelected;

    String floorRoomData;

    Bitmap roomPhoto;

    boolean buildingSelectedFilled = false;

    boolean floorRoomDataFilled = false;

    int nextRoomId;
    //

    TextView selectFloor;

    public static int buildingPosition = 0;

    public static int floorPosition = 0;

    ProgressBar progressBar;

    public static int PHOTO_REQUEST_CODE = 232;

    TextView selectRoomNumber;

    Integer rotation = 90;

    Uri lastPhotoUri;

    Map<String, String> buildings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_add);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        chooseBuild = (Spinner) findViewById(R.id.buildingToRoom);
        chooseFloor = (Spinner) findViewById(R.id.floorToRoom);
        roomNumber = (EditText) findViewById(R.id.numberInputText);
        progressBar = (ProgressBar) findViewById(R.id.addRoomProgress);
        choosePhoto = (FloatingActionButton) findViewById(R.id.floatingActionButton3);
        photoToRoom = (ImageView) findViewById(R.id.roomPhotoToChoose);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        addButton = (Button) findViewById(R.id.button4);
        selectFloor = (TextView) findViewById(R.id.textView12);
        selectRoomNumber = (TextView) findViewById(R.id.textView14);

        chooseBuild.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position > 0) {
                    buildingSelectedFilled = true;
                    buildingPosition = position;
                    buildingSelected = (String) parent.getItemAtPosition(position);
                    selectFloor.setVisibility(View.VISIBLE);
                    chooseFloor.setVisibility(View.VISIBLE);

                    adapterForFloors = new ArrayAdapter<String>(RoomAdd.this, android.R.layout.simple_spinner_item);

                    adapterForFloors.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    adapterForFloors.add("Choose..");

                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDINGS_KEY).child(buildings.get(buildingSelected)).child("floors").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot sp : dataSnapshot.getChildren()) {
                                adapterForFloors.add(sp.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    chooseFloor.setAdapter(adapterForFloors);

                } else {
                    buildingSelectedFilled = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        chooseFloor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (floorPosition > 0) {
                    chooseFloor.setSelection(floorPosition);
                }
                if (position > 0) {
                    floorRoomDataFilled = true;
                    floorRoomData = (String) parent.getItemAtPosition(position);
                    roomNumber.setVisibility(View.VISIBLE);
                    selectRoomNumber.setVisibility(View.VISIBLE);
                    choosePhoto.setVisibility(View.VISIBLE);
                    photoToRoom.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.VISIBLE);
                    addButton.setVisibility(View.VISIBLE);
                    floorPosition = position;

                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.FLOORS_KEY).child(floorRoomData + ":" + buildingSelected).child("rooms").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Integer> list = new LinkedList<>();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                list.add(Integer.parseInt(ds.getKey()));
                            }

                            for (int i = 0; i < list.size() - 1; i++) {
                                nextRoomId = Math.max(list.get(i), list.get(i + 1));
                            }
                            nextRoomId++;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    floorRoomDataFilled = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    final int room = Integer.parseInt(roomNumber.getText().toString().equals("") == false ? roomNumber.getText().toString() : "-1");

                    if (floorRoomDataFilled && buildingSelectedFilled && roomPhoto != null && room > 0 ) {
                        objRoom.setRoomNumber(room);
                        objRoom.setFloorId(floorRoomData + ":" + buildings.get(buildingSelected));
                        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot sp : dataSnapshot.getChildren()) {

                                    if (sp.child("email").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                        objRoom.setOwnerId(sp.getKey());
                                        objRoom.setRoomId(nextRoomId);
                                        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.FLOORS_KEY).child(floorRoomData + ":" + buildings.get(buildingSelected)).child("rooms").child(objRoom.getRoomId() + "").setValue(objRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).child(objRoom.getOwnerId());

                                                userDataRef.child("roomId").setValue(objRoom.getRoomId());
                                                userDataRef.child("floorId").setValue(floorRoomData + ":" + buildings.get(buildingSelected));

                                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("rooms").child(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                                roomPhoto.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                                byte [] photoInBytes = byteArrayOutputStream.toByteArray();
                                                storageReference.putBytes(photoInBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        Intent intent = new Intent(RoomAdd.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }

                                        });
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(RoomAdd.this, "Fill all fields please", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RoomAdd.this, "Sorry, but it is not your room", Toast.LENGTH_LONG).show();
                }
            }
        });

        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RoomAdd.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(RoomAdd.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    String [] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(RoomAdd.this, permissions, 1);
                } else {
                    Intent getPhotoIntent = new Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(getPhotoIntent, "Room photo"), PHOTO_REQUEST_CODE);
                }
            }
        });
        photoToRoom.setOnTouchListener(new View.OnTouchListener() {

            private GestureDetector gestureDetector = new GestureDetector(RoomAdd.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    rotation = (rotation + 90) % 360;
                    getPhotoFromMemory(lastPhotoUri);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    public void getPhotoFromMemory(final Uri uri) {
        lastPhotoUri = uri;
        progressBar.setVisibility(View.VISIBLE);
        final int width = photoToRoom.getWidth();
        final int height = photoToRoom.getHeight();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    roomPhoto = Picasso.with(RoomAdd.this).load(uri).resize(width, height).rotate(rotation).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            photoToRoom.setImageBitmap(roomPhoto);
                            progressBar.setVisibility(View.INVISIBLE);
                            chooseBuild.setSelection(buildingPosition);
                            Toast.makeText(RoomAdd.this, "double tap to rotate image", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RoomAdd.this, "Error while loading photo", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_CODE) {
            getPhotoFromMemory(data.getData());
        }
    }
//TODO 1 my status and my qr code, my chats, save to gallery qr

    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.add("Select..");
        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.BUILDINGS_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    String key = s.getKey();
                    String address = s.child("address").getValue(String.class);
                    if (key != null && address != null) {
                        buildings.put(address, key);
                        arrayAdapter.add(address);
                    }
                    chooseBuild.setAdapter(arrayAdapter);
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
