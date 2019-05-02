package com.example.eventerapp;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventerapp.entity.Message;
import com.example.eventerapp.service.MessageNotificationJobService;
import com.example.eventerapp.utils.DatabaseContract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobTrigger;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    EditText messageField;

    ImageButton sendButton;

    ImageView joinIcon;

    Button joinChat;

    CardView joinCard;

    ImageView addedIcon;

    String email;

    String photoUrl;

    Animator animator;

    String idOfEvent;

    List <Integer> membersIds = new LinkedList<>();

    String name;

    public static FirebaseJobDispatcher dispatcher;

    public static String lastId;

    public static String lastName;

    public static boolean isActive;

    @Override
    protected void onResume() {
        super.onResume();
        checkAsRead();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    public void checkAsRead() {
        FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<Integer> numbers = new ArrayList<>();

                String userId = "";

                for (DataSnapshot s : dataSnapshot.child(DatabaseContract.USER_DATA_KEY).getChildren()) {
                    if (s.child("email").getValue(String.class).equals(email)) {
                        userId = s.getKey();
                        break;
                    }
                }

                for (DataSnapshot s : dataSnapshot.child(DatabaseContract.CHAT_DATA_KEY).child(idOfEvent).getChildren()) {

                    if (s.getKey().equals("members") || s.getKey().equals("next_id")) {
                        continue;
                    }

                    numbers.add(Integer.parseInt(s.getKey()));
                }

                int max = 0;

                for (int i = 0; i < numbers.size() - 1; i++) {
                    max = Math.max(numbers.get(i), numbers.get(i + 1));
                }

                FirebaseDatabase.getInstance().getReference("notification").child(userId).child(idOfEvent).setValue(max);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        joinCard = findViewById(R.id.joingAskCard);
        joinChat = findViewById(R.id.joinButton);
        joinIcon = findViewById(R.id.joinIcon);
        addedIcon = findViewById(R.id.addedMemberAnimIcon);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseStorage.getInstance().getReference().child("users").child(email).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                  photoUrl = uri.toString();
            }
        });

        messageField = findViewById(R.id.chat_input);
        sendButton = findViewById(R.id.imageButton2);

        recyclerView = findViewById(R.id.chat_messages_box);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ChatAdapter(this, new ArrayList<Message>()));

        Intent intent = getIntent();
        final String id = intent.getStringExtra("id") == null ? lastId : intent.getStringExtra("id");
        name = intent.getStringExtra("name") == null ? lastName : intent.getStringExtra("name");

        if (id != null && name != null) {
            actionBar.setTitle(name);
            lastId = id;
            lastName = name;
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }

        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (isActive) {
                    checkAsRead();
                }

                boolean isJoined = false;

                for (DataSnapshot s : dataSnapshot.child("chat").child(id).child("members").getChildren()) {
                    if (dataSnapshot.child(DatabaseContract.USER_DATA_KEY).child(s.getValue(Long.class) + "").child("email").getValue(String.class).equals(email)) {
                        isJoined = true;
                    }
                }

                if (!isJoined) {
                    showJoinDialog();
                }

                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.child("chat").child(id).getChildren()) {
                    if (snapshot.getKey().equals("next_id") || snapshot.getKey().equals("members")) {
                        continue;
                    }
                    messages.add(new Message(snapshot.child("id").getValue(Long.class), snapshot.child("author").getValue(String.class), snapshot.child("messageText").getValue(String.class), snapshot.child("photoUrl").getValue(String.class)));
                }

                messages.sort(new Comparator<Message>() {
                    @Override
                    public int compare(Message o1, Message o2) {
                        return (int) (o1.getId() - o2.getId());
                    }
                });

                recyclerView.setAdapter(new ChatAdapter(ChatActivity.this, messages));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        idOfEvent = id;

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendMessage(messageField.getText().toString());
            }
        });

        joinChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot sp : dataSnapshot.getChildren()) {

                            if (sp.child("email").getValue(String.class).equals(email)) {

                                try {

                                    Integer id = Integer.parseInt(sp.getKey());
                                    FirebaseDatabase.getInstance().getReference().child("chat").child(idOfEvent).child("members").push().setValue(id);

                                    dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(ChatActivity.this));
                                    Bundle eventData = new Bundle();
                                    eventData.putString("eventId", idOfEvent);
                                    eventData.putString("eventName", name);
                                    eventData.putString("userId", sp.getKey());
                                    Job job = dispatcher.newJobBuilder()
                                            .setService(MessageNotificationJobService.class)
                                            .setLifetime(Lifetime.FOREVER)
                                            .setConstraints(Constraint.ON_ANY_NETWORK)
                                            .setRecurring(true)
                                            .setExtras(eventData)
                                            .setReplaceCurrent(true)
                                            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                                            .setTag(name + "-chat")
                                            .setTrigger(Trigger.executionWindow(0, 10))
                                            .build();

                                    dispatcher.schedule(job);

                                    closeJoinDialog();

                                    break;

                                } catch (Exception e) {
                                    Toast.makeText(ChatActivity.this, "Something wrong.. Sorry...", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (item.getItemId() == R.id.membersDetails) {
                FirebaseDatabase.getInstance().getReference().child("chat").child(idOfEvent + "").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for ( DataSnapshot ds : dataSnapshot.child("members").getChildren()) {
                            membersIds.add(ds.getValue(Long.class).intValue());
                        }
                        if (membersIds.size() == 0) {
                            Toast.makeText(ChatActivity.this, "0 members", Toast.LENGTH_LONG).show();
                        } else {
                            Intent detailsMemebers = new Intent(ChatActivity.this, MembersActivity.class);
                            int memIds [] = new int[membersIds.size()];

                            for (int i = 0; i < membersIds.size(); i++) {
                                memIds[i] = membersIds.get(i);
                            }
                            detailsMemebers.putExtra("members", memIds);
                            detailsMemebers.putExtra("id", idOfEvent);
                            detailsMemebers.putExtra("name", name);
                            startActivity(detailsMemebers);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    public void showJoinDialog() {
        joinIcon.setVisibility(View.VISIBLE);
        joinChat.setVisibility(View.VISIBLE);
        joinCard.setVisibility(View.VISIBLE);

    }

    public void closeJoinDialog() {
        joinIcon.setVisibility(View.INVISIBLE);
        joinChat.setVisibility(View.INVISIBLE);
        joinCard.setVisibility(View.INVISIBLE);

        addedIcon.setVisibility(View.VISIBLE);

        animator = AnimatorInflater.loadAnimator(this, R.animator.member_added);
        animator.setTarget(addedIcon);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                addedIcon.setVisibility(View.INVISIBLE);
                sendMessage("Hello everybody! I am in ;)");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void sendMessage(String text) {
        if (photoUrl != null) {

            final String messageTextStr = text;
            messageField.setText("");

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chat").child(idOfEvent);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long nextId = dataSnapshot.child("next_id").getValue(Long.class);
                    Message message = new Message(nextId, email, messageTextStr, photoUrl);
                    reference.child(nextId + "").setValue(message);
                    reference.child("next_id").setValue(++nextId);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(ChatActivity.this, "Sorry, wait few seconds", Toast.LENGTH_LONG).show();
        }
    }

}
