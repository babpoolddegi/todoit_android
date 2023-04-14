package com.example.doit;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImgUrl;
    TextView username;

    public FriendListHolder(@NonNull View itemView) {
        super(itemView);
        profileImgUrl = itemView.findViewById(R.id.profileImage2);
        username = itemView.findViewById(R.id.username2);
    }
}
