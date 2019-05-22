package com.example.eventerapp.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.example.eventerapp.ChatActivity;
import com.example.eventerapp.R;
import com.example.eventerapp.utils.DatabaseContract;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageNotificationJobService extends JobService {

    AsyncTask asyncTask;

    Context context;

    PendingIntent pendingIntent;

    private static final int REQUEST_CODE_FOR_PENDING_INTENT = 1;

    private static final int NOTIFICATION_ID = 3;

    @Override
    public boolean onStartJob(JobParameters job) {
        final JobParameters jobCopy = job;
        context = getApplicationContext();
        final String userId = job.getExtras().getString("userId");
        final String eventId = jobCopy.getExtras().getString("eventId");
        final String eventName = jobCopy.getExtras().getString("eventName");
        asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                final String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                context = getApplicationContext();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Integer lastMessageId = dataSnapshot.child("notification").child(userId).child(eventId).getValue(Long.class).intValue();

                        Intent intentForChat = new Intent(context, ChatActivity.class);
                        intentForChat.putExtra("id", eventId);
                        intentForChat.putExtra("name", eventName);

                        pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE_FOR_PENDING_INTENT, intentForChat, PendingIntent.FLAG_UPDATE_CURRENT);

                        final Integer currentMessageId = lastMessageId == null ? -1 : lastMessageId;

                                List<String> newMessages = new ArrayList<>();

                                List<Integer> messagesIds = new ArrayList<>();

                                for (DataSnapshot snapshot : dataSnapshot.child(DatabaseContract.CHAT_DATA_KEY).child(eventId).getChildren()) {

                                    if (snapshot.getKey().equals("members") || snapshot.getKey().equals("next_id") || snapshot.child("author").getValue(String.class).equals(userEmail)) {
                                        continue;
                                    }

                                    int idOfMsg = Integer.parseInt(snapshot.getKey());

                                    if (idOfMsg > currentMessageId) {
                                        newMessages.add(snapshot.child("author").getValue(String.class) + ": " + snapshot.child("messageText").getValue(String.class));
                                        messagesIds.add(idOfMsg);
                                    }

                                }
                                if ( newMessages.size() > 0 ) {
                                    int max = 0;

                                    for (int i = 0; i < messagesIds.size() - 1; i++) {
                                        max = Math.max(messagesIds.get(i), messagesIds.get(i + 1));
                                    }

                                    FirebaseDatabase.getInstance().getReference().child("notification").child(userId).child(eventId).setValue(max);


                                    String messageText = null;

                                    if (newMessages.size() == 1) {

                                        messageText = newMessages.get(0);

                                    } else if (newMessages.size() > 1) {

                                        messageText = "There are " + newMessages.size() + " new messages";

                                    }

                                    if (messageText != null) {

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Messages")
                                                .setAutoCancel(true)
                                                .setContentIntent(pendingIntent)
                                                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                                                .setContentTitle("Messages in " + eventName)
                                                .setSmallIcon(R.drawable.blur)
                                                .setColor(getColor(android.R.color.holo_blue_light))
                                                .setContentText(messageText)
                                                .setColorized(true);

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                                        notificationManager.notify(NOTIFICATION_ID, builder.build());

                                    }
                                }


                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                return null;

                }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobCopy, false);
                super.onPostExecute(o);
            }
        };

        asyncTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        return true;
    }
}
