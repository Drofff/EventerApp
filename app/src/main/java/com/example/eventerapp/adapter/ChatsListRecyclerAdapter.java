package com.example.eventerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventerapp.R;
import com.example.eventerapp.activity.ChatActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsListRecyclerAdapter extends RecyclerView.Adapter<ChatsListRecyclerAdapter.ChatViewHolder> {

    List<Map<String, String>> chats = new LinkedList<>();

    Context context;

    public ChatsListRecyclerAdapter(Context context, List<Map<String, String>> chats) {
        this.chats = chats;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ChatViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_list_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
        Map<String, String> chatMaps = chats.get(i);
        chatViewHolder.fill(context, chatMaps.get("id"), chatMaps.get("photoUrl"), chatMaps.get("title"), chatMaps.get("lastMessage"), chatMaps.get("newMessages"), chatMaps.get("ownerEmail"));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        CircleImageView photoOfChat;

        TextView titleOfChat;

        TextView lastMessageFromChat;

        ConstraintLayout chat;

        TextView newMessages;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.chat = itemView.findViewById(R.id.fieldOfChat);
            this.photoOfChat = itemView.findViewById(R.id.photoOfChat);
            this.titleOfChat = itemView.findViewById(R.id.titleOfChat);
            this.lastMessageFromChat = itemView.findViewById(R.id.lastMessageFromChat);
            this.newMessages  = itemView.findViewById(R.id.lastMessagesCounter);
        }

        public void fill(final Context context, final String id, String photoUri, final String title, String lastMessage, String newMessages, String ownerEmail) {

            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("name", title);
                    context.startActivity(intent);
                }
            });

            try {
                Integer newMsgs = Integer.parseInt(newMessages);

                if (newMsgs > 0) {
                    this.newMessages.setVisibility(View.VISIBLE);
                    this.newMessages.setText(newMsgs + "");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            titleOfChat.setText(title);
            lastMessageFromChat.setText(lastMessage);

            FirebaseStorage.getInstance().getReference().child("rooms").child(ownerEmail).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(photoOfChat);
                }
            });

        }

    }

}
