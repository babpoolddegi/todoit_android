package com.example.doit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {

    FirebaseRecyclerOptions<UserModel> options;
    FirebaseRecyclerAdapter<UserModel, FindFriendViewHolder> adapter;

    DatabaseReference mUserRef, requestRef, friendRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    RecyclerView recyclerView;
    Dialog dilaog01; // 커스텀 다이얼로그

    String userId;
    String profileImgeUrl, userName,fprofileImgeUrl,fuserName;

    String currendState = "nothing_happen";
    Button acbtn, dlbtn;
    ImageButton btn_x;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        dilaog01 = new Dialog(FindFriendActivity.this);       // Dialog 초기화
        dilaog01.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dilaog01.setContentView(R.layout.activity_view_friend);

        recyclerView = findViewById(R.id.friendview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUserRef = FirebaseDatabase.getInstance().getReference().child("profile_img");
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
                Toast.makeText(FindFriendActivity.this, "검색 처리됨 : " + query, Toast.LENGTH_SHORT).show();
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


        Query query = mUserRef.orderByChild("userName").startAt(s).endAt(s + "\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<UserModel>().setQuery(query, UserModel.class).build();

        adapter = new FirebaseRecyclerAdapter<UserModel, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull UserModel model) {
                if (!mUser.getUid().equals(getRef(position).getKey().toString())) {
                    Picasso.get().load(model.getProfileImgUrl()).into(holder.profileImgUrl);
                    holder.username.setText(model.getUserName());
                    Log.d("DB TEST","name2:"+ holder.username);
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
                        showDialog01(userId);
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

    // dialog01을 디자인하는 함수
    public void showDialog01(String userId) {

        CircleImageView profileImage = dilaog01.findViewById(R.id.circleImageView);
        TextView pusername = dilaog01.findViewById(R.id.userName);

        acbtn = dilaog01.findViewById(R.id.acceptbtn);
        dlbtn = dilaog01.findViewById(R.id.declinebtn);
        btn_x = dilaog01.findViewById(R.id.btn_x);

        dilaog01.show(); // 다이얼로그 띄우기
        dilaog01.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        acbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAction(userId);
                dilaog01.dismiss();
            }
        });
        CheckUserExistance(userId);
        dlbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                unfriend(userId);
                dilaog01.dismiss();
            }
        });
        // x버튼 클릭했을 때 다이얼로그를 닫아줌.
        btn_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dilaog01.dismiss();
            }
        });

        // 친구 프로필 사진과 정보 불러오는 부분
        mUserRef = FirebaseDatabase.getInstance().getReference().child("profile_img").child(userId);

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    profileImgeUrl = snapshot.child("profileImgUrl").getValue().toString();
                    userName = snapshot.child("userName").getValue().toString();

                    Picasso.get().load(profileImgeUrl).into(profileImage);//다이얼로그 이미지 넣기
                    pusername.setText(userName);//다이얼로그 텍스트 넣기

                } else {
                    Toast.makeText(FindFriendActivity.this, "Data Not Found", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ======================================================친구 삭제===================================
    private void unfriend(String userId) {

        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        // 친구추가 취소할 때
        HashMap hashMapUnFriend = new HashMap<>();
        requestRef.child(mUser.getUid()).child(userId).child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString().equals("I_sent_pending")){
                    HashMap hashMapUnFriend = new HashMap<>();
                    requestRef.child(mUser.getUid()).child(userId).removeValue();
                    Toast.makeText(FindFriendActivity.this,"취소 되었습니다.",Toast.LENGTH_SHORT).show();

                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //친구 추가 거절할 때
        requestRef.child(userId).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    HashMap hashMapUnFriend = new HashMap<>();
                    requestRef.child(userId).child(mUser.getUid()).removeValue();
                    requestRef.child(mUser.getUid()).child(userId).removeValue();
                    acbtn.setText("친구 요청");
                    dlbtn.setText("취소");
                    Toast.makeText(FindFriendActivity.this,"거절 되었습니다.",Toast.LENGTH_SHORT).show();


                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //친구 삭제
        //친구 추가 거절할 때
        friendRef.child(mUser.getUid()).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    friendRef.child(mUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                friendRef.child(userId).child(mUser.getUid()).removeValue();
                                acbtn.setText("친구 요청");
                                dlbtn.setText("취소");
                                Toast.makeText(FindFriendActivity.this,"삭제 되었습니다.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    HashMap hashMapUnFriend = new HashMap<>();

                }else {

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //===========================친구 수락 메소드==============================
    private void CheckUserExistance(String userId) {

        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        friendRef.child(mUser.getUid()).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    acbtn.setText("메시지 보내기");
                    dlbtn.setText("친구삭제");
                    acbtn.setVisibility(View.VISIBLE);
                    dlbtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        friendRef.child(userId).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    acbtn.setText("메시지 보내기");
                    dlbtn.setText("친구삭제");
                    acbtn.setVisibility(View.VISIBLE);
                    dlbtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //============================친구 상태===============================

        //==========================================친구수락 부분==========================
        requestRef.child(userId).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {

                        HashMap hashMap = new HashMap<>();
                        hashMap.put("current", "he_sent_pending");

                        requestRef.child(mUser.getUid()).child(userId).updateChildren(hashMap);
                        acbtn.setText("친구 수락");
                        dlbtn.setText("친구 거절");
                        dlbtn.setVisibility(View.VISIBLE);
                    }
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // ===========================버튼 친구추가 안보냈을 시에 기본 화면====================
        friendRef.child(mUser.getUid()).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {


                }else{
                    acbtn.setText("친구 요청");
                    dlbtn.setText("취소");
                    acbtn.setVisibility(View.VISIBLE);
                    dlbtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //=========================================친구 추가 요청 메소드==============================
    private void performAction(String userId) {
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("profile_img");


        requestRef.child(mUser.getUid()).child(userId).child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){


                }else {
                    HashMap hashMap = new HashMap<>();
                    hashMap.put("current", "I_sent_pending");
                    hashMap.put("status", "pending");
                    requestRef.child(mUser.getUid()).child(userId).updateChildren(hashMap);
                    dlbtn.setVisibility(View.VISIBLE);
                    Toast.makeText(FindFriendActivity.this,"친구 요청 완료",Toast.LENGTH_SHORT).show();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //친구가 보낸 친구 요청
        requestRef.child(mUser.getUid()).child(userId).child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    if(snapshot.getValue().toString().equals("he_sent_pending")){
                        requestRef.child(mUser.getUid()).child(userId).child("current").removeValue();
                        requestRef.child(userId).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("status","friend");
                                    hashMap.put("username",userName);
                                    hashMap.put("profileImageUrl",profileImgeUrl);

                                    friendRef.child(mUser.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()){
                                                HashMap hashMap2 = new HashMap();
                                                hashMap2.put("status","friend");
                                                hashMap2.put("username",fuserName);
                                                hashMap2.put("profileImageUrl",fprofileImgeUrl );
                                                Log.d("Test0444:", ""+fprofileImgeUrl );

                                                friendRef.child(userId).child(mUser.getUid()).updateChildren(hashMap2).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        Toast.makeText(FindFriendActivity.this,"친구추가 완료",Toast.LENGTH_SHORT).show();

                                                        acbtn.setText("sSend SNS");
                                                        dlbtn.setText("UnFriend");
                                                        dlbtn.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // 친구 데이터 넣는 코드
        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    fuserName = snapshot.child("userName").getValue().toString();
                    fprofileImgeUrl= snapshot.child("profileImgUrl").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}