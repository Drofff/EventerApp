package com.example.eventerapp;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
        chatViewHolder.fill(context, chatMaps.get("photoUrl"), chatMaps.get("title"), chatMaps.get("lastMessage"));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        CircleImageView photoOfChat;

        TextView titleOfChat;

        TextView lastMessageFromChat;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            this.photoOfChat = itemView.findViewById(R.id.photoOfChat);
            this.titleOfChat = itemView.findViewById(R.id.titleOfChat);
            this.lastMessageFromChat = itemView.findViewById(R.id.lastMessageFromChat);
        }

        public void fill(final Context context, String photoUri, String title, String lastMessage) {
            titleOfChat.setText(title);
            lastMessageFromChat.setText(lastMessage);
            FirebaseStorage.getInstance().getReference().child("users").child(photoUri).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(photoOfChat);
                }
            });
        }

    }

}
