package com.example.eventerapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventerapp.R;
import com.example.eventerapp.entity.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {

    Context context;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;

    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    List<Message> messageList;

    String currentUser;

    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = (Message) messageList.get(position);

        if (message.getAuthor().equals(currentUser)) {
            return VIEW_TYPE_MESSAGE_SENT;
        }

        return VIEW_TYPE_MESSAGE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == VIEW_TYPE_MESSAGE_SENT) {
            return new ViewHolderForChar(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup, false));
        } else {
            return new ViewHolderForReceivedMessages(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.received_message_layout, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        Message msg = messageList.get(i);

        boolean sentMessage = msg.getAuthor().equals(currentUser)? true: false;

        if (sentMessage) {
            ViewHolderForChar viewHolderForChar = (ViewHolderForChar) viewHolder;
            viewHolderForChar.setText(msg.getMessageText());
        } else {
            ViewHolderForReceivedMessages viewHolderForReceivedMessages = (ViewHolderForReceivedMessages) viewHolder;
            viewHolderForReceivedMessages.setMessage(msg.getMessageText(), msg.getAuthor(), msg.getPhotoUrl());
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ViewHolderForChar extends RecyclerView.ViewHolder {

        TextView messageText;

        public ViewHolderForChar(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.myMessage);
        }

        public void setText(String text) {
            this.messageText.setText(text);
        }
    }

    class ViewHolderForReceivedMessages extends RecyclerView.ViewHolder {

        TextView messageText;

        ImageView authorPhoto;

        public ViewHolderForReceivedMessages(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.textView9);
            authorPhoto = itemView.findViewById(R.id.authorPhoto);

        }

        public void setMessage(String text, String authorName, String photoUrl) {
            messageText.setText(text);
            Glide.with(context).load(photoUrl).into(authorPhoto);
        }
    }
}
