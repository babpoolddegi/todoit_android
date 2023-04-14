package com.example.doit;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {
    private List<Friends> friendsList;

    public FriendsAdapter(List<Friends> friendsList){
        this.friendsList = friendsList;
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder{

        TextView tvNickname;
        CircleImageView image;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNickname = itemView.findViewById(R.id.tvnickname);
            image = itemView.findViewById(R.id.profile_image);
        }
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friends, parent, false);
        FriendsViewHolder friendsViewHolder = new FriendsViewHolder(view);
        return friendsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {

        Friends friends = friendsList.get(position);
        Picasso.get().load(friends.getProfileImageUrl()).into(holder.image);
        holder.tvNickname.setText(friends.getUsername());
    }

    @Override
    public int getItemCount() {
        return friendsList==null?0:friendsList.size();
    }
}