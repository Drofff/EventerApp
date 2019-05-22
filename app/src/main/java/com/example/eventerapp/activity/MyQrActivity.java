package com.example.eventerapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.eventerapp.R;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MyQrActivity extends AppCompatActivity {

    private String roomPath;

    QRGEncoder qrgEncoder;

    private static final String DIR_PHOTO = "/Pictures";

    ImageView qrImage;

    ProgressBar progressBar;

    public static final int PERM_REQ_CODE = 345;

    public static Bitmap qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Your QR");

        qrImage = findViewById(R.id.qrCodeImageView);
        progressBar = findViewById(R.id.myQrProgessBar);

        final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.child(DatabaseContract.USER_DATA_KEY).getChildren()) {
                        if (snapshot.child("email").getValue(String.class).equals(email)) {
                            roomPath = snapshot.child("floorId").getValue(String.class) + ":" + snapshot.child("roomId").getValue(Long.class);
                            break;
                        }
                    }

                    int smaller = ( qrImage.getWidth() < qrImage.getHeight() ? qrImage.getWidth() : qrImage.getHeight() ) * 3/4;
                    qrgEncoder = new QRGEncoder(roomPath, null, QRGContents.Type.TEXT, smaller);
                    generate();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    public void generate() {
        try {
            qrCode = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(qrCode);
            qrImage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MyQrActivity.this, "Error", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(MyQrActivity.this);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERM_REQ_CODE) {
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), qrCode, "my_qr_code", "QR code for eventer app for your room");
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error while saving", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (item.getItemId() == R.id.save_to_gal) {
            if (qrCode == null) {
                Toast.makeText(this, "Please, wait", Toast.LENGTH_SHORT).show();
            } else {
                try {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        MediaStore.Images.Media.insertImage(getContentResolver(), qrCode, "my_qr_code", "QR code for eventer app for your room");
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        String [] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        ActivityCompat.requestPermissions(this, perms, PERM_REQ_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error while saving", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_qr_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
