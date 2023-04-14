package com.example.doit;

import static com.example.doit.CustomDialog.customDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doit.databinding.ActivityNavigationBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInAccount gsa;
    private Button btnLogoutGoogle;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef;
    String profileImgUrlV;
    String userNameV;
    CircleImageView myProfile;
    TextView userName;

    String gProfileV;
    String gNameV;

    private List<Friends> friendsList = new ArrayList<>();

    // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    //DatabaseReference는 데이터베이스의 특정 위치로 연결하는 거라고 생각하면 된다.
    //현재 연결은 데이터베이스에만 딱 연결해놓고
    //키값(테이블 또는 속성)의 위치 까지는 들어가지는 않은 모습이다.
    private DatabaseReference databaseReference = database.getReference();
    private FirebaseUser user;

    //네비게이션 메뉴 추가부분
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityNavigationBinding binding;

    //프로필 이미지 텍스트 변경에 쓸 친구들
    //프로필 이미지 텍스트 변경에 쓸 친구들
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    CircleImageView circleView;
    TextView Username, Email;
    private final int GALLERY_CODE =200;
    private final int gallayImage = 200;
    private Uri imgUri;
    private String pathUri;

    String date;
    private String uid;
    private List<todoList> listDto = new ArrayList<>();
    private List<String> uidList = new ArrayList<>(); //게시물 key
    private List<RepeatList> repeatList = new ArrayList<>();
    private String updateKey;

    HashMap<String, Object> listUpdate = new HashMap<String, Object>();

    Dialog dialogProfile;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);


        //=======================네비게이션 메뉴 추가부분==================
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarNavigation.toolbar);
        //toolbar title 제거
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                //페이지 구성
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //==========================네비게이션===========================

        //=================네비게이션 프로픨 다이얼로그 연결==================
        dialogProfile = new Dialog(MainActivity.this);
        dialogProfile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogProfile.setContentView(R.layout.activity_profile);  // xml파일과 연결

        //===================네비게이션 프로픨 다이얼로그 끝==================

        //============================프로필=============================
        View header = navigationView.getHeaderView(0);
        circleView = (CircleImageView) header.findViewById(R.id.CircleView);
        Username = header.findViewById(R.id.UserName);
        Email = header.findViewById(R.id.Email);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //메뉴프로필 (사진,이름 이메일) ///프로필 추가
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {  // 구글 로그인 했을 때
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();
            Log.d("name", ""+name);
            Log.d("email", ""+email);
            Log.d("photoUrl", ""+photoUrl);

            Username.setText(name);
            Email.setText(email);
            Picasso.get().load(photoUrl).into(circleView);

        }


        //프로필사진 클릭시 갤러리에서 사진 변경
        circleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadAlbum();


            }
        });
//===============================================프로필==========================

        //---------------Google 로그인------------------------
        firebaseAuth = FirebaseAuth.getInstance();
        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //---------------Google 로그인------------------------//

        //--------------캘린더 위 myProfile 띄우기-------------//
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child("profile_img");
        myProfile = findViewById(R.id.myProfile);
        userName = findViewById(R.id.userName);
        //--------------캘린더 위 myProfile 띄우기-------------//

        //---------------메인 캘린더---------------------------//
        CalendarView calendarView = findViewById(R.id.calendarView);
        Button btn_save = findViewById(R.id.save_Btn);
        Button btn_updte = findViewById(R.id.update_Btn);
        EditText editText = findViewById(R.id.contextEditText);
        TextView repeatText = findViewById(R.id.repeatText);

        // todoList에 대한 RecyclerView
        RecyclerView recyclerView1 = findViewById(R.id.recyclerView1);
       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView1.setLayoutManager(linearLayoutManager);
        todoListAdapter adapter = new todoListAdapter(listDto, uidList, MainActivity.this);

        // friends에 대한 RecyclerView
        RecyclerView recyclerFriends = findViewById(R.id.recyclerFriends);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerFriends.setLayoutManager(linearLayoutManager1);
        FriendsAdapter fadapter = new FriendsAdapter(friendsList);

        // 메인화면에 뿌릴 친구 리스트
        databaseReference.child("Friends").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                friendsList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Friends friendsDto = ds.getValue(Friends.class);
                    Log.d("image", friendsDto+"");
//                    profileImgUrlV = ds.child("profileImgUrl").getValue().toString();
//                    userNameV = snapshot.child("userName").getValue().toString();
//                    String status = snapshot.child("status").getChildren().toString();
//
//                    friendsDto.setProfileImgUrl(profileImgUrlV);
//                    friendsDto.setStatus(status);
//                    friendsDto.setUsername(userNameV);

                    friendsList.add(friendsDto);
                }
                recyclerFriends.setAdapter(fadapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // RepeatList에 대한 RecyclerView
        RecyclerView recyclerRepeat = findViewById(R.id.recyclerRepeat);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerRepeat.setLayoutManager(linearLayoutManager2);
        RepeatListAdapter radapter = new RepeatListAdapter(repeatList);


        editText.setVisibility(View.VISIBLE);
        btn_save.setVisibility(View.VISIBLE);

        // 날짜 기본값(클릭을 하지 않으면 오늘날짜가 들어감)
        Calendar cal = Calendar.getInstance();
        Date nowDate = cal.getTime();
        SimpleDateFormat dataformat = new SimpleDateFormat("yyyy-MM-dd");
        date = dataformat.format(nowDate);

        //다른 날짜를 클릭하면
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {

                // 날짜 계산
                if(month<9 && dayOfMonth<10)
                    date = year + "-0" + (month + 1) + "-0" + dayOfMonth;
                else if(month>=9 && dayOfMonth<10)
                    date = year + "-" + (month + 1) + "-0" + dayOfMonth;
                else if(month<9 && dayOfMonth>=10)
                    date = year + "-0" + (month + 1) + "-" + dayOfMonth;
                else if(month>=9 && dayOfMonth>=10)
                    date = year + "-" + (month + 1) + "-" + dayOfMonth;

                // 반복할일에 등록된 것을 뿌려줌..
                database.getReference().child("repeatList").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //변화된 값이 DataSnapshot 으로 넘어온다.
                        //데이터가 쌓이기 때문에  clear()
                        repeatList.clear();
                        uidList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RepeatList repeatListDto = ds.getValue(RepeatList.class);
                            String uidKey = ds.getKey();
                            try {
                                Date date1 = dataformat.parse(date);
                                int compare1 = date1.compareTo(nowDate);
                                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(repeatListDto.getUid())) {
                                    if (compare1 >= 0) {
                                        repeatList.add(repeatListDto);
                                        uidList.add(uidKey);
                                        repeatText.setVisibility(View.VISIBLE);
                                    } else {
                                        repeatText.setVisibility(View.GONE);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        recyclerRepeat.setAdapter(radapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                dateList(recyclerView1, adapter);
            }
        });

        // 오늘 날짜에 입력된 값을 뿌려주기 위해 사용
        dateList(recyclerView1, adapter);

        // 저장 버튼을 클릭하면
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                }
                addList(editText.getText().toString(), date, 0, uid);
                editText.setText("");
            }
        });

        adapter.setListener(new todoListListener() {
            @Override
            public void onItemClick(int position, String key) {
                updateKey = key;
                todoList todolist = adapter.getItem(position);
                editText.setText(todolist.getContent());
                btn_save.setVisibility(View.GONE);
                btn_updte.setVisibility(View.VISIBLE);
            }

        });
        btn_updte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listUpdate.put("content", editText.getText().toString());
                listUpdate.put("date", date);

                //HashMap에 저장된 값으로 변경됨.(값이 없으면 DB에 값이 추가됨)
                database.getReference().child("todoList").child(updateKey).updateChildren(listUpdate);

                editText.setText("");
                btn_save.setVisibility(View.VISIBLE);
                btn_updte.setVisibility(View.GONE);
            }
        });

        // 네비게이션 메뉴
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_gallery: // 네비게이션 메뉴안에 있는 프로필
                        showDialogProfile();
                        break;

                    case R.id.nav_Logout:  // 네비게이션 메뉴안에 있는 로그아웃
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                        Log.d("usertest :", user + "usertest");
                        signOut(); //로그아웃
                        break;

                    case R.id.nav_DoitList: // 네비게이션 메뉴안에 있는 할 일 목록
                        Intent intent = new Intent(getApplicationContext(), DoitList.class);
                        startActivity(intent);
                        break;

                    case R.id.nav_repeat:  // 네비게이션 메뉴안에 있는 반복적인 일
                        Intent intent1 = new Intent(getApplicationContext(), ListRepeat.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_Update: // 네비게이션 메뉴 안에 있는 정보수정 탭으로 이동
                        Intent intent2 = new Intent(getApplicationContext(), ProfileUpdate.class);
                        startActivity(intent2);
                        break;
                    case R.id.friends:
                        Intent friends = new Intent(getApplicationContext(), FriendListActivity.class);
                        startActivity(friends);

                }

                return false;
            }
        });


    }//~~oncreate

    //EditText에 있는 값을 파이어베이스 Realtime database로 넘기는 함수
        public void addList (String content, String date,int chkId, String uid){
            todoList todoList = new todoList(content, date, chkId, uid, 0);
            databaseReference.child("todoList").push().setValue(todoList);
        }

        // 파이어베이스에서 데이터 가져옴
        //옵저버 패턴 --> 변화가 있으면 클라이언트에 알려준다.
        // 데이터를 달력 밑에 출력해주는 함수(날짜별로)
        public void dateList (RecyclerView recyclerView1, todoListAdapter adapter){
            database.getReference().child("todoList").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //변화된 값이 DataSnapshot 으로 넘어온다.
                    //데이터가 쌓이기 때문에  clear()
                    listDto.clear();
                    uidList.clear();

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        todoList todolistDto = ds.getValue(todoList.class);
                        String uidKey = ds.getKey();

                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(todolistDto.getUid()) && date.equals(todolistDto.getDate())) {
                            listDto.add(todolistDto);
                            uidList.add(uidKey);
                        }
                    }
                    recyclerView1.setAdapter(adapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //메뉴(하트,서치) 추가
        @Override
        public boolean onOptionsItemSelected (@NonNull MenuItem item){


            switch (item.getItemId()) {
//                case R.id.action_Like:
//                    Intent likeIntent = new Intent(this, LikeActivity.class);
//                    startActivity(likeIntent);
//                    break;

                case R.id.action_Search:
                    Intent searchIntent = new Intent(this, FindFriendActivity.class);
                    startActivity(searchIntent);
                    break;

            }

            return super.onOptionsItemSelected(item);

        }

        //네비게이션  추가부분
        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.navigation, menu);
            return true;
        }
        //네비게이션  추가부분
        @Override
        public boolean onSupportNavigateUp () {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();

        }

    ////////////////////////////////////추가부분//////////////////////////
    private void loadAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_CODE);
    }
    // uri 절대경로 가져오기
    private String getPath(Uri uri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground();
        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();
        return cursor.getString(index);
    }

    //프로필
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE) {
            imgUri = data.getData();
            pathUri = getPath(data.getData());
        }
        Log.d("yong", "반복222");

        Uri file = Uri.fromFile(new File(pathUri));
        StorageReference storageRef = mStorage.getInstance().getReference().child("profile_img").child("uid/" + file.getLastPathSegment());
        storageRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();

                while (!imageUrl.isComplete());

                mDatabase.getInstance().getReference().child("profile_img").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String usernames = snapshot.child("userName").getValue().toString();
                        String uid = firebaseAuth.getCurrentUser().getUid();


                        UserModel userModel = new UserModel();
                        userModel.uid = uid;
                        userModel.userName = usernames;
                        userModel.profileImgUrl = imageUrl.getResult().toString();
                        mDatabase.getInstance().getReference().child("profile_img").child(uid).setValue(userModel);
                        Picasso.get().load(imgUri).into(circleView);

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private void RetrieveUserInfo() {
        NavigationView navigationView = binding.navView;
        View header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.UserName);

        FirebaseUser mFirebaseUser = firebaseAuth.getInstance().getCurrentUser();
        if(mFirebaseUser != null) {
            uid = mFirebaseUser.getUid(); //Do what you need to do with the id
        }

        mDatabase.getInstance().getReference().child("profile_img").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("profileImgUrl"))) {
                   String usernames = snapshot.child("userName").getValue().toString();
                    username.setText(usernames);

                    String ProfileImage = snapshot.child("profileImgUrl").getValue().toString();
                    Picasso.get().load(ProfileImage).into(circleView);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    // 네비게이션-프로필 선택시 dialog 뜨는 함수
        public void showDialogProfile() {
            CustomDialog.getInstance(this).showDialog();
            CircleImageView p_civ = customDialog.findViewById(R.id.p_civ);
            TextView p_userName = customDialog.findViewById(R.id.p_userName);
            TextView friendsCount = customDialog.findViewById(R.id.friendsCount);
            TextView heartCount = customDialog.findViewById(R.id.heartCount);

            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        profileImgUrlV = snapshot.child("profileImgUrl").getValue().toString();
                        userNameV = snapshot.child("userName").getValue().toString();
                        Picasso.get().load(profileImgUrlV).into(p_civ);
                        p_userName.setText(userNameV);
                        friendsCount.setText(friendsList.size()+" 명");
                        heartCount.setText("3 개");
                    } else if(mUser.getPhotoUrl() != null){
                        try {
                            URL url = new URL(mUser.getPhotoUrl().toString());
                            gProfileV = url.toString();
                            gNameV = mUser.getDisplayName();
                            Picasso.get().load(gProfileV).into(p_civ);
                            p_userName.setText(gNameV);
                            friendsCount.setText("2 명");
                            heartCount.setText("5 개");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // 프로필 메인 화면에 뿌려줌
        @Override
        protected void onStart () {
            super.onStart();
            RetrieveUserInfo();
            if (mUser == null) {
                SendUserToLoginActivity();
            } else {
                mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            profileImgUrlV = snapshot.child("profileImgUrl").getValue().toString();
                            userNameV = snapshot.child("userName").getValue().toString();
                            Picasso.get().load(profileImgUrlV).into(myProfile);
                            userName.setText(userNameV);
                        }
                        else if(mUser.getPhotoUrl() != null){
                            gProfileV = mUser.getPhotoUrl().toString();
                            gNameV = mUser.getDisplayName();
                            Picasso.get().load(gProfileV).into(myProfile);
                            userName.setText(gNameV);
                        }

                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }

        private void SendUserToLoginActivity () {
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        }

        /* 로그아웃 */
        private void signOut () {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, task -> {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d("usertest :", user + "");
                        updateUI(user);

                        firebaseAuth.signOut();
                        Toast.makeText(MainActivity.this, R.string.success_logout, Toast.LENGTH_SHORT).show();
                    });
            gsa = null;
        }
        //    로그아웃 화면 전환
        private void updateUI (FirebaseUser user){ //update ui code here
            if (user != null) {
                Intent intent = new Intent(this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }