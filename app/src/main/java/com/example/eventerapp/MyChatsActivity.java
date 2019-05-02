package com.example.eventerapp;

import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.eventerapp.utils.DatabaseContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyChatsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chats);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        recyclerView = findViewById(R.id.chatsRV);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(new ChatsListRecyclerAdapter(this, new LinkedList<Map<String, String>>()));

        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map<String, String> userData = new HashMap<>();

                List<Map<String, String>> chatList = new LinkedList<>();

                String myId = null;

                for (DataSnapshot snapshot : dataSnapshot.child(DatabaseContract.USER_DATA_KEY).getChildren()) {
                    String currentEmail = snapshot.child("email").getValue(String.class);
                    userData.put(snapshot.getKey(), currentEmail);
                    if (currentEmail.equals(email)) {
                        myId = snapshot.getKey();
                    }
                }

                if (myId != null) {

                    long lastMessageId = 0;

                    for (DataSnapshot snapshot : dataSnapshot.child("chat").getChildren()) {

                        Map<String, String> chats = new HashMap<>();

                        if (snapshot.child("members").child(myId).exists()) {

                            chats.put("title", dataSnapshot.child(DatabaseContract.EVENTS_KEY).child(snapshot.getKey()).child("title").getValue(String.class));

                            for (DataSnapshot ds : snapshot.getChildren()) {

                                if (ds.getKey().equals("members") || ds.getKey().equals("next_id")) {
                                    continue;
                                }

                                long currentId = Long.parseLong(ds.getKey());

                                if (currentId > lastMessageId) {
                                    lastMessageId = currentId;
                                }

                            }

                            chats.put("lastMessage", snapshot.child(lastMessageId + "").child("messageText").getValue(String.class));
                            chats.put("photoUrl", snapshot.child(lastMessageId + "").child("photoUrl").getValue(String.class));

                            chatList.add(chats);

                        }

                    }

                    if (chatList.size() == 0) {
                        Toast.makeText(MyChatsActivity.this, "You are not member of any chat", Toast.LENGTH_SHORT).show();
                        NavUtils.navigateUpFromSameTask(MyChatsActivity.this);
                    } else {
                        recyclerView.setAdapter(new ChatsListRecyclerAdapter(MyChatsActivity.this, chatList));
                    }

                } else {

                    Toast.makeText(MyChatsActivity.this, "Non existing user", Toast.LENGTH_SHORT).show();
                    NavUtils.navigateUpFromSameTask(MyChatsActivity.this);

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
