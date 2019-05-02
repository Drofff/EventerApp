package com.example.eventerapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eventerapp.utils.DatabaseContract;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MembersActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    Button leaveChatButton;

    String idOfEvent;

    String nameOfEvent;

    String myId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        leaveChatButton = findViewById(R.id.leaveChatButton);

        Intent intent = getIntent();

        nameOfEvent = intent.getStringExtra("name");
        idOfEvent = intent.getStringExtra("id");

        if (idOfEvent == null || nameOfEvent == null) {
            Intent intentToMain = new Intent(this, HomePage.class);
            startActivity(intentToMain);
        }


        recyclerView = findViewById(R.id.membersListDetails);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        final int [] membersIds = intent.getIntArrayExtra("members");

        System.out.println("Look what I've received");

        if (membersIds == null || membersIds.length == 0) {
            NavUtils.navigateUpFromSameTask(this);
        }

        actionBar.setTitle(Html.fromHtml("<font color='white'>Members: " + membersIds.length + "</font>"));

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<String> members = new ArrayList<>();

                for (int id : membersIds) {
                    String email = dataSnapshot.child(id + "").child("email").getValue(String.class);
                    if (email != null) {
                        members.add(email);
                    }


                    if (email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        myId = id + "";
                    }

                }

                if (myId == null) {
                    leaveChatButton.setText("You are not members of this chat");
                } else {
                    leaveChatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chat").child(idOfEvent).child("members");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot sp : dataSnapshot.getChildren()) {
                                            if ( sp.getValue().toString().equals(myId)) {
                                                ref.child(sp.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Intent toMain = new Intent(MembersActivity.this, HomePage.class);

                                                        if (ChatActivity.dispatcher != null) {
                                                            ChatActivity.dispatcher.cancel(nameOfEvent + "-chat");
                                                        } else {
                                                            new FirebaseJobDispatcher(new GooglePlayDriver(MembersActivity.this)).cancel(nameOfEvent + "-chat");

                                                        }

                                                        startActivity(toMain);
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                        }
                    });
                }

                recyclerView.setAdapter(new MembersAdapter(members, MembersActivity.this));
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
