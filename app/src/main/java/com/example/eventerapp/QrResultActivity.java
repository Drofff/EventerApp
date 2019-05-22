package com.example.eventerapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventerapp.entity.Event;
import com.example.eventerapp.entity.Message;
import com.example.eventerapp.entity.Room;
import com.example.eventerapp.service.MessageNotificationJobService;
import com.example.eventerapp.utils.DatabaseContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QrResultActivity extends AppCompatActivity {

    TextView textView;

    ImageView errorIcon;

    ImageView addedMembersIcon;

    EditText phoneNumber;

    EditText title;

    EditText description;

    TextView titleText;

    TextView descText;

    TextView phoneText;

    Button addNewEvent;

    ProgressBar progressBar;

    Long nextEventId = 0l;

    String roomOwner = "";

    Long userId = -1l;

    Message message;

    boolean exists = false;

    boolean free = false;

    String eventId = "";

    public void showEventAddForm() {
        title.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        titleText.setVisibility(View.VISIBLE);
        descText.setVisibility(View.VISIBLE);
        phoneText.setVisibility(View.VISIBLE);
        addNewEvent.setVisibility(View.VISIBLE);
        phoneNumber.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_result);

        textView = findViewById(R.id.textView8);
        progressBar = findViewById(R.id.progressBar4);
        errorIcon = findViewById(R.id.imageView7);
        addedMembersIcon = findViewById(R.id.member_added_icon_qr);
        titleText = findViewById(R.id.titleAddEv);
        title = findViewById(R.id.editText5);
        phoneNumber = findViewById(R.id.editText4);
        description = findViewById(R.id.editText6);
        descText = findViewById(R.id.descTextEvAdd);
        phoneText = findViewById(R.id.textView15);
        addNewEvent = findViewById(R.id.addNewEventButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        Intent intent = getIntent();
        final String code = intent.getStringExtra("code");

        if (code != null) {
            String [] dataFromCode = code.split(":");
            if (dataFromCode.length == 3) {

                final String floorId = dataFromCode[0] + ":" + dataFromCode[1];
                final String roomId = dataFromCode[2];


                FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.INVISIBLE);
                        if (dataSnapshot.child(DatabaseContract.FLOORS_KEY).child(floorId).child("rooms").child(roomId).getChildrenCount() > 0) {

                            try {

                                roomOwner = dataSnapshot.child(DatabaseContract.FLOORS_KEY).child(floorId).child("rooms").child(roomId).child("ownerId").getValue(String.class);

                                for (DataSnapshot snapshot : dataSnapshot.child(DatabaseContract.USER_DATA_KEY).getChildren()) {
                                    if (snapshot.child("email").getValue(String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                        userId = Long.parseLong(snapshot.getKey());
                                        if (snapshot.child("currentPostion").getValue(String.class).equals("free")) {
                                            free = true;
                                        }
                                        break;
                                    }
                                }

                                List<Long> ids = new ArrayList<>();

                                for (DataSnapshot snapshot : dataSnapshot.child(DatabaseContract.EVENTS_KEY).getChildren()) {

                                    ids.add(Long.parseLong(snapshot.getKey()));

                                    Long roomIdOfThis = snapshot.child("roomId").getValue(Long.class);
                                    String floorIdOfThis = snapshot.child("floorId").getValue(String.class);

                                    if ( ( roomIdOfThis == Long.parseLong(roomId) ) && floorIdOfThis.equals(floorId)) {
                                        exists = true;
                                        eventId = snapshot.getKey();
                                        break;
                                    }

                                }

                                Long max = 0l;

                                for (Long i : ids) {
                                    if (i > max) {
                                        max = i;
                                    }
                                }

                                nextEventId = ++max;


                            } catch (Exception e) {
                                e.printStackTrace();
                                codeError();

                            }

                            System.out.println("EVENT EXISTS? " + exists);
                            System.out.println("USER ID " + userId);
                            System.out.println("FREE? " + free);



                            if (exists && userId >= 0 && !eventId.equals("")) {


                                textView.setVisibility(View.VISIBLE);

                                if (free) {


                                    addedMembersIcon.setVisibility(View.VISIBLE);
                                    textView.setText("Done! Have a nice time!");

                                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).child(eventId).child("members").child(userId + "").setValue(true);

                                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).child(userId + "").child("currentPostion").setValue("member");

                                } else {


                                    errorIcon.setVisibility(View.VISIBLE);
                                    textView.setText("Sorry, you are at another event now");

                                }

                            } else if (!exists && userId >= 0 && free) {


                                if ( roomOwner != null && userId != null && roomOwner.equals("" + userId) ) {


                                    final Long finalUserId = userId;

                                    addedMembersIcon.setVisibility(View.INVISIBLE);
                                    textView.setVisibility(View.INVISIBLE);
                                    showEventAddForm();
                                    addNewEvent.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (title.getText() != null && title.getText().length() > 5) {
                                                if (phoneNumber.getText() != null) {
                                                    if (description.getText() != null && description.getText().length() > 10) {

                                                        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).child(finalUserId + "").child("currentPostion").setValue("host");

                                                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                                        Event event = new Event();
                                                        event.setContactPhone(phoneNumber.getText().toString());
                                                        event.setDescription(description.getText().toString());
                                                        event.setFloorId(floorId);
                                                        event.setOwnerEmail(email);
                                                        event.setTitle(title.getText().toString());
                                                        event.setRoomId(Long.parseLong(roomId));
                                                        event.setStartDate(new Date().toString());

                                                        Map<String, Boolean> members = new HashMap<>();
                                                        members.put(userId + "", true);
                                                        event.setMembers(members);
                                                        event.setMyId(nextEventId);
                                                        FirebaseDatabase.getInstance().getReference().child(DatabaseContract.EVENTS_KEY).child(nextEventId + "").setValue(event);

                                                        message = new Message();
                                                        message.setAuthor(email);
                                                        message.setId(0l);
                                                        message.setMessageText("Welcome!");

                                                        FirebaseStorage.getInstance().getReference().child("users").child(email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                message.setPhotoUrl(uri.toString());

                                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chat").child(nextEventId + "");

                                                                ref.child(0 + "").setValue(message);
                                                                ref.child("next_id").setValue(1);
                                                                ref.child("members").push().setValue(finalUserId);

                                                                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(QrResultActivity.this));
                                                                Bundle eventData = new Bundle();
                                                                eventData.putString("eventId", nextEventId + "");
                                                                eventData.putString("eventName", title.getText().toString());
                                                                eventData.putString("userId", finalUserId + "");
                                                                Job job = dispatcher.newJobBuilder()
                                                                        .setService(MessageNotificationJobService.class)
                                                                        .setLifetime(Lifetime.FOREVER)
                                                                        .setConstraints(Constraint.ON_ANY_NETWORK)
                                                                        .setRecurring(true)
                                                                        .setExtras(eventData)
                                                                        .setReplaceCurrent(true)
                                                                        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                                                                        .setTag(title.getText().toString() + "-chat")
                                                                        .setTrigger(Trigger.executionWindow(0, 10))
                                                                        .build();

                                                                dispatcher.schedule(job);

                                                                FirebaseDatabase.getInstance().getReference("notification").child(finalUserId + "").child(nextEventId + "").setValue(0l);

                                                                Toast.makeText(QrResultActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(QrResultActivity.this, HomePage.class);
                                                                startActivity(intent);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                codeError();
                                                            }
                                                        });


                                                    } else {
                                                        Toast.makeText(QrResultActivity.this, "Description must be larger than 10 chars", Toast.LENGTH_LONG).show();
                                                        phoneNumber.setForeground(new ColorDrawable(Color.RED));
                                                    }
                                                } else {
                                                    Toast.makeText(QrResultActivity.this, "Please, enter your number", Toast.LENGTH_LONG).show();
                                                    phoneNumber.setForeground(new ColorDrawable(Color.RED));
                                                }
                                            } else {
                                                Toast.makeText(QrResultActivity.this, "Title must be longer than 5 chars", Toast.LENGTH_LONG).show();
                                                title.setForeground(new ColorDrawable(Color.RED));
                                            }
                                        }
                                    });

                                } else {
                                    errorIcon.setVisibility(View.VISIBLE);
                                    textView.setVisibility(View.VISIBLE);
                                    textView.setText("Event do not exists");
                                }

                            } else {
                                errorIcon.setVisibility(View.VISIBLE);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText("Invalid qr code");

                            }

                        } else {
                            errorIcon.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("Invalid qr code");

                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            } else {
                codeError();
            }
        } else {
            codeError();
        }
    }

    public void codeError() {
        Toast.makeText(this, "Invalid Code", Toast.LENGTH_LONG).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
