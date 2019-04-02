package com.example.eventerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventerapp.entity.Event;
import com.example.eventerapp.entity.Room;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>{

    List<Event> events = new LinkedList<>();

    Context context;


    public RoomsAdapter(Context context, List<Event> events) {

        this.context = context;
        this.events = events;
    }

    public void setRooms(List<Event> rooms) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RoomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.room_item, viewGroup, false), context);
    }

    @Override
    public void onBindViewHolder(@NonNull final RoomViewHolder roomViewHolder, int i) {
        final Event event = events.get(i);
        if (event != null) {
            roomViewHolder.setTitle(event.getTitle());
            if (event.getOwnerEmail() != null) {
                System.out.println("I AM IN IF");
                FirebaseStorage.getInstance().getReference().child("rooms").child(event.getOwnerEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        roomViewHolder.setRoomPhoto(uri);
                    }
                });
            }
            roomViewHolder.setEventReference(event.getMyId() + "");
            roomViewHolder.setMembersCount(event.getMembers().size());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseContract.FLOORS_KEY).child(event.getFloorId()).child("rooms").child("" + event.getRoomId()).child("roomNumber");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    long roomNumber = dataSnapshot.getValue(Long.class);
                    roomViewHolder.setRoomNumber(roomNumber + "");
                    FirebaseDatabase.getInstance().getReference().child(DatabaseContract.USER_DATA_KEY).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            int count = 0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                if (event.getMembers().containsKey(Long.parseLong(snapshot.getKey()))) {

                                    count++;

                                    StorageReference refer = FirebaseStorage.getInstance().getReference().child("users");

                                    if (count == 1) {
                                        refer.child(snapshot.child("email").getValue(String.class)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                roomViewHolder.setMember1Photo(uri);
                                            }
                                        });
                                    } else if (count == 2) {
                                        refer.child(snapshot.child("email").getValue(String.class)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                roomViewHolder.setMember2Photo(uri);
                                            }
                                        });
                                    } else if (count == 3) {
                                        refer.child(snapshot.child("email").getValue(String.class)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                roomViewHolder.setMember3Photo(uri);
                                            }
                                        });
                                    }

                                }

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {

        CircleImageView roomPhoto;

        TextView roomNumber;

        TextView membersCountTextView;

        Context context;

        TextView title;

        CircleImageView member1;

        CircleImageView member2;

        CircleImageView member3;

        public RoomViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            title = (TextView) itemView.findViewById(R.id.textView7);
            roomPhoto = (CircleImageView) itemView.findViewById(R.id.roomPhotoImageCircle);
            roomNumber = (TextView) itemView.findViewById(R.id.roomNumber);
            membersCountTextView = (TextView) itemView.findViewById(R.id.membersCount);
            member1 = (CircleImageView) itemView.findViewById(R.id.member1);
            member2 = (CircleImageView) itemView.findViewById(R.id.member2);
            member3 = (CircleImageView) itemView.findViewById(R.id.member3);

        }

        public void setMember1Photo(Uri m1) {
            Picasso.with(context).load(m1).resize(member1.getWidth(), member1.getHeight()).centerCrop().into(member1);
        }

        public void setMember2Photo(Uri m2) {
            Picasso.with(context).load(m2).resize(member2.getWidth(), member2.getHeight()).centerCrop().into(member2);
        }

        public void setMember3Photo(Uri m3) {
            Picasso.with(context).load(m3).resize(member3.getWidth(), member3.getHeight()).centerCrop().into(member3);;
        }

        public void setRoomNumber(String number) {
            roomNumber.setText("Room: " + number);
        }

        public void setMembersCount(int size) {
            if (size == 1) {
                membersCountTextView.setText(size + " member");
            } else {
                membersCountTextView.setText(size + " members");
            }
        }

        public void setRoomPhoto(Uri photo) {
            Picasso.with(context).load(photo).resize(roomPhoto.getWidth(), roomPhoto.getHeight()).centerCrop().into(roomPhoto);
        }

        public void setTitle(String title) {
            this.title.setText(title);
        }

        public void setEventReference(final String eventId) {
            itemView.findViewById(R.id.roomItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, EventActivity.class);
                    intent.putExtra("id", eventId);
                    context.startActivity(intent);
                }
            });
        }

    }
}
