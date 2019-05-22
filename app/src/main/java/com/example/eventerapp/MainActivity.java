package com.example.eventerapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventerapp.entity.UserData;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "138828966830-trgjlhf73gahssul8fjloj93n0nochef.apps.googleusercontent.com";

    Button logInButton;

    GoogleSignInClient client;

    DatabaseReference db;

    GoogleSignInAccount googleSignInAccount;

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    private static final int LOGIN_ACTIVITY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        logInButton = (Button) findViewById(R.id.logInButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        HomePage.photosViewModel = ViewModelProviders.of(this).get(PhotosViewModel.class);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder()
                .requestEmail()
                .requestIdToken("48290241841-vbekm7bl3j6h7vjg9jr3ekfcac38l3n9.apps.googleusercontent.com")
                .build();

        client = GoogleSignIn.getClient(this, options);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = client.getSignInIntent();
                startActivityForResult(loginIntent, LOGIN_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY) {
            Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
                firebaseLogin(googleSignInAccount);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Authorizaton failed", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void login() {
        db = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                boolean isNew = true;

                int lastKey = 0;

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    lastKey = Integer.parseInt(ds.getKey()) + 1;

                    String emailOfThisOne = ds.child("email").getValue(String.class);

                    if (ds.hasChild("email") && emailOfThisOne != null && emailOfThisOne.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        isNew = false;
                    }
                }

                if (isNew) {

                            Executors.newSingleThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ByteArrayOutputStream outputStreamForPhoto = new ByteArrayOutputStream();
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        Picasso.with(MainActivity.this).load(user.getPhotoUrl()).get().compress(Bitmap.CompressFormat.PNG, 100, outputStreamForPhoto);
                                        FirebaseStorage.getInstance().getReference().child("users").child(user.getEmail()).putBytes(outputStreamForPhoto.toByteArray());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    UserData userData = new UserData();
                    userData.setCurrentPostion("free");
                    userData.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    db.child(lastKey + "").setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    Intent intent = new Intent(MainActivity.this, HomePage.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void firebaseLogin(final GoogleSignInAccount account) {
        final AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    login();
                } else {
                    Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
           Intent intent = new Intent(this, HomePage.class);
           startActivity(intent);
        }
    }
}
