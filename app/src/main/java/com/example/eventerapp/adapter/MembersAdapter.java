package com.example.eventerapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eventerapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

    List<String> members = new ArrayList<>();

    Context context;

    public MembersAdapter(final List<String> members, Context context) {
        this.members = members;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MemberViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_details, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder memberViewHolder, int i) {
        String currentEmail = members.get(i);
        memberViewHolder.setNameOfMember(currentEmail);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        TextView memberName;

        CircleImageView photoMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.nameOfMember);
            photoMember = itemView.findViewById(R.id.photoOfMember);
        }

        public void setNameOfMember(String name) {
            memberName.setText(name);
            FirebaseStorage.getInstance().getReference().child("users").child(name).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(photoMember);
                }
            });
        }

    }

}
