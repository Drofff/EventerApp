package com.example.eventerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class CameraActivity extends AppCompatActivity {

    FrameLayout frameLayout;
    ImageButton backToMain;
    CameraSource cam;
    boolean done = false;

    public static final int PERMISSION_GRANTED_REQUEST_CODE = 434;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        backToMain = findViewById(R.id.backToHome);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(CameraActivity.this);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            String [] perm = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this, perm, PERMISSION_GRANTED_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERMISSION_GRANTED_REQUEST_CODE) {
            startCamera();
        }
    }

    public void startCamera() {
        cam = getCamera();
        CameraPreview cameraPreview = new CameraPreview(this, cam);
        frameLayout = findViewById(R.id.preview_container);
        frameLayout.addView(cameraPreview);
    }


    public CameraSource getCamera() {
        CameraSource camera = null;
        try {
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(CameraActivity.this).setBarcodeFormats(Barcode.QR_CODE).build();
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
                    if (barcodeSparseArray.size() > 0 && done == false) {
                        done = true;
                        Barcode barcode = barcodeSparseArray.valueAt(0);
                        Intent intent = new Intent(CameraActivity.this, QrResultActivity.class);
                        intent.putExtra("code", barcode.displayValue);
                        startActivity(intent);
                    }
                }
            });
            camera = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).build();
        } catch (Exception e) {
            Toast.makeText(this, "Camera is used by other application", Toast.LENGTH_LONG).show();
        }
        return camera;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cam.release();
    }
}
