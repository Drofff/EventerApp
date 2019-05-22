package com.example.eventerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eventerapp.entity.Event;
import com.example.eventerapp.utils.DatabaseContract;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>{

    List<Event> events = new LinkedList<>();

    Context context;

    private static PhotosViewModel photosViewModel;


    public RoomsAdapter(Context context, List<Event> events, PhotosViewModel photosViewModel) {

        this.context = context;
        this.events = events;
        this.photosViewModel = photosViewModel;
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
                FirebaseStorage.getInstance().getReference().child("rooms").child(event.getOwnerEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Bitmap cacheRoomPhoto = photosViewModel.getPhoto(PhotosViewModel.ROOM_PHOTO, uri.toString(), roomViewHolder.roomPhoto.getWidth(), roomViewHolder.roomPhoto.getHeight());
                        if (cacheRoomPhoto != null) {
                            roomViewHolder.roomPhoto.setImageBitmap(cacheRoomPhoto);
                        } else {
                            roomViewHolder.setRoomPhoto(uri);
                        }
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
                    FirebaseStorage.getInstance().getReference().child("users").child(event.getOwnerEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            roomViewHolder.setOwnerPhoto(uri);
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

        ImageView roomPhoto;

        TextView roomNumber;

        TextView membersCountTextView;

        Context context;

        TextView title;

        CircleImageView ownerPhoto;

        public RoomViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            title = (TextView) itemView.findViewById(R.id.textView7);
            roomPhoto = (ImageView) itemView.findViewById(R.id.roomPhotoImageCircle);
            roomNumber = (TextView) itemView.findViewById(R.id.room_nuimber_event);
            membersCountTextView = (TextView) itemView.findViewById(R.id.membersCount);
            ownerPhoto = (CircleImageView) itemView.findViewById(R.id.owner_of_event);

        }

        public void setOwnerPhoto(Uri m1) {
            Glide.with(context).load(m1).apply(new RequestOptions().override(ownerPhoto.getWidth(), ownerPhoto.getHeight())).centerCrop().into(ownerPhoto);
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
                Glide.with(context).load(photo).apply(new RequestOptions().override(roomPhoto.getWidth(), roomPhoto.getHeight())).centerCrop().into(roomPhoto);
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
