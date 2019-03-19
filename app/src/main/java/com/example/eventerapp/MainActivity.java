package com.example.eventerapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "138828966830-trgjlhf73gahssul8fjloj93n0nochef.apps.googleusercontent.com";

    FloatingActionButton logInButton;

    GoogleSignInClient client;

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    private static final int LOGIN_ACTIVITY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        logInButton = (FloatingActionButton) findViewById(R.id.button);


        actionBar = getSupportActionBar();
        actionBar.setTitle(Html.fromHtml("<font color = 'gray'>Eventer</font>"));
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        Animator animatorSet = AnimatorInflater.loadAnimator(this, R.animator.login_button);
        animatorSet.setTarget(logInButton);
        animatorSet.start();

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
                GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
                firebaseLogin(googleSignInAccount);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Authorizaton failed", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void login() {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
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
           login();
        }
    }
}
