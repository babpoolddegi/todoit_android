package com.example.doit;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class FriendListActivity extends AppCompatActivity {

    FirebaseRecyclerOptions<FriendModel> options;
    FirebaseRecyclerAdapter<FriendModel, FindFriendViewHolder> adapter;

    String userId;

    DatabaseReference mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        recyclerView = findViewById(R.id.friendview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        LoadUser("");

        //==========================검색==========================
        SearchView searchView;
        searchView = findViewById(R.id.search_btn);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색 버튼이 눌러졌을 때 이벤트 처리
                LoadUser(query);
                Toast.makeText(FriendListActivity.this, "검색 처리됨 : " + query, Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 검색어가 변경되었을 때 이벤트 처리
                LoadUser(newText);
                return false;


            }
        });
        //========검색==============

    }//~~~onCreate

    private void LoadUser(String s) {

        Query query = mUserRef.child(mUser.getUid()).orderByChild("username").startAt(s).endAt(s + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<FriendModel>().setQuery(query, FriendModel.class).build();

        adapter = new FirebaseRecyclerAdapter<FriendModel, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull FriendModel model) {
                if (!mUser.getUid().equals(getRef(position).getKey().toString())) {
                    Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImgUrl);
                    holder.username.setText(model.getUsername());
                    Log.d("DB TEST", "name2:" + holder.username);
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FriendModel friendModel = new FriendModel();
                        userId = getRef(position).getKey().toString();
                        friendModel.setUserID(userId);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_view_find_friend, parent, false);
                return new FindFriendViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }
}
