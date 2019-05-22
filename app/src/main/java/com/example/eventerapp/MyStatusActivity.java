package com.example.eventerapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyStatusActivity extends AppCompatActivity {

    CircleImageView userPhoto;

    TextView status;

    FloatingActionButton goToChat;

    FloatingActionButton goToEventPage;

    Button leaveCurrentEvent;

    String currentPostion;

    String userId;

    String eventId;

    String eventName;

    boolean isMember;

    private static final String MEMBER_KEY = "member";

    private static final String HOST_KEY = "host";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_status);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);

        status = findViewById(R.id.statusText);

        userPhoto = findViewById(R.id.imageView5);

        goToChat = findViewById(R.id.chatOfEventButton);

        goToEventPage = findViewById(R.id.eventPageGoToButton);

        leaveCurrentEvent = findViewById(R.id.leaveStatusButton);

        final String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        FirebaseStorage.getInstance().getReference().child("users").child(email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Glide.with(MyStatusActivity.this).load(uri).into(userPhoto);
                } catch (Exception e) {
                    System.out.println("Glide error");
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for ( DataSnapshot sp : dataSnapshot.getChildren() ) {
                    if (sp.child("email").getValue(String.class).equals(email)) {
                        setCurrentPostion(sp.child("currentPostion").getValue(String.class), sp.getKey());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void setCurrentPostion(String currentPostion, String userId) {
        this.currentPostion = currentPostion;
        this.userId = userId;
        status.setText(currentPostion.toUpperCase());

        if (! currentPostion.equals("free")) {
            setRefs(currentPostion);
        }
    }

    public void setRefs(String currentPostion) {
        goToChat.setVisibility(View.VISIBLE);
        goToEventPage.setVisibility(View.VISIBLE);
        leaveCurrentEvent.setVisibility(View.VISIBLE);

        isMember = false;

        if (currentPostion.equals(MEMBER_KEY)) {
            isMember = true;
        } else if (currentPostion.equals(HOST_KEY)) {
            leaveCurrentEvent.setText("CANCEL");
        }

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("members").child(userId).exists()) {
                        eventId = snapshot.getKey();
                        eventName = snapshot.child("title").getValue(String.class);

                        final long numberOfMembers = snapshot.child("members").getChildrenCount();

                        goToChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MyStatusActivity.this, ChatActivity.class);
                                intent.putExtra("id", eventId);
                                intent.putExtra("name", eventName);
                                startActivity(intent);
                            }
                        });

                        goToEventPage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MyStatusActivity.this, EventActivity.class);
                                intent.putExtra("id", eventId);
                                startActivity(intent);
                            }
                        });

                        leaveCurrentEvent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).child(userId).child("currentPostion").setValue("free");

                                if (isMember && numberOfMembers > 2) {
                                    removeMember(eventId, userId);
                                } else {
                                    removeEvent(eventId);
                                }
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

    public void removeMember(String eventId, String memberId) {
        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).child(eventId).child("members").child(memberId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MyStatusActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MyStatusActivity.this, HomePage.class);
                startActivity(intent);
            }
        });
    }

    public void removeEvent(final String eventId) {

        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).child(eventId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseDatabase.getInstance().getReference().child(DatabaseContract.CHAT_DATA_KEY).child(eventId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MyStatusActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MyStatusActivity.this, HomePage.class);
                        startActivity(intent);
                    }
                });

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
