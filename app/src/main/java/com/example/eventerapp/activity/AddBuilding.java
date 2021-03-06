package com.example.eventerapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.eventerapp.R;
import com.example.eventerapp.entity.Building;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class AddBuilding extends AppCompatActivity {

    ImageButton loadImageButton;

    static Bitmap photo;

    String uriToBuildingPhoto = null;

    private static final int CONTENT_GET_KEY = 22;

    private static ProgressBar progressBar;

    Button addBuilding;

    FirebaseDatabase database;

    EditText addressField;

    EditText numberOfFloors;

    Integer rotation = 90;

    Uri lastPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");


        database = FirebaseDatabase.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        addBuilding = (Button) findViewById(R.id.addBuildingB);
        addressField = (EditText) findViewById(R.id.editText);
        numberOfFloors = (EditText) findViewById(R.id.editText2);

        loadImageButton = (ImageButton) findViewById(R.id.button2);
        loadImageButton.setColorFilter(Color.WHITE);

        loadImageButton.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(AddBuilding.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    rotation = rotation == 360 ? 90 : rotation + 90;
                    getPhotoFromMemory(lastPhotoUri);
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (ContextCompat.checkSelfPermission(AddBuilding.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(AddBuilding.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        String [] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(AddBuilding.this, permissions, 1);
                    } else {
                        Intent intent = new Intent().setType("image/*")
                                .setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,  "Choose a image"),  CONTENT_GET_KEY);
                    }
                    return true;
                }
            });
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        addBuilding = (Button) findViewById(R.id.addBuildingB);
        addBuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address = addressField.getText().toString();
                database.getReference().child("buildings").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            String addressExs = data.child("address").getValue(String.class);
                            if (addressExs.equals(address)) {
                                Toast.makeText(AddBuilding.this, "Address already exists", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if (address != null && address.length() > 3) {

                    Long numberOfRooms = 0l;

                    try {
                        numberOfRooms = Long.parseLong(numberOfFloors.getText().toString());
                    } catch (Exception e) {
                        Toast.makeText(AddBuilding.this, "Only numbers are allowed", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Map<String, Boolean> floors = new HashMap<>();
                    for (Long i = 1l; i <= numberOfRooms; i++) {
                        floors.put(i.toString(), true);
                    }

                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();


                    if (photo != null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference reference = storage.getReference().child("images").child("buildingBY" + email);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte [] data = byteArrayOutputStream.toByteArray();
                        UploadTask task = reference.putBytes(data);
                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                uriToBuildingPhoto = reference.getDownloadUrl().toString();
                            }
                        });
                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddBuilding.this, "Error while uploading photo", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    final Building building = new Building(address, uriToBuildingPhoto, floors, email);
                    progressBar.setVisibility(View.VISIBLE);
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            database.getReference().child("buildings").push().setValue(building).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(AddBuilding.this, HomePage.class);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    final Exception f = e;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            f.printStackTrace();
                                            Toast.makeText(AddBuilding.this, "Error while inserting data", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                        }
                    });

                } else {
                    Toast.makeText(AddBuilding.this, "Address can't be so small", Toast.LENGTH_LONG).show();
                }

            }
        });



    }

    private void getPhotoFromMemory(final Uri uri) {
        lastPhotoUri = uri;
        progressBar.setVisibility(View.VISIBLE);
        final int width = loadImageButton.getWidth();
        final int height = loadImageButton.getHeight();
        try {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        photo = Picasso.with(AddBuilding.this).load(uri).rotate(rotation).resize(width, height).centerCrop().get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadImageButton.setColorFilter(null);
                                loadImageButton.setImageBitmap(photo);
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddBuilding.this, "double tap to rotate image", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(AddBuilding.this, "Error while loading photo", Toast.LENGTH_LONG).show();
                            }
                        });
                        e.printStackTrace();
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Can't load image", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Uri uri = data.getData();
        if (requestCode == CONTENT_GET_KEY) {
            getPhotoFromMemory(uri);
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
