package com.example.doit;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileUpdate extends AppCompatActivity {

    private final int PICK_FROM_ALBUM = 10;

    // 파이어베이스 데이터베이스 연동
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    //DatabaseReference는 데이터베이스의 특정 위치로 연결하는 거라고 생각하면 된다.
    //현재 연결은 데이터베이스에만 딱 연결해놓고
    private DatabaseReference databaseReference = database.getReference();
    private FirebaseUser user;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private String pathUri;
    private File tempFile;
    private Uri imgUri;

    CircleImageView profile;
    Dialog dialog01, dialog02;  // 커스텀 다이얼로그
    EditText uEmail, uName;
    Button btnUpdate, btnDelete;

    String gProfileV;
    String gNameV;

    private GoogleSignInAccount gsa;
    private GoogleSignInClient mGoogleSignInClient;

    // 데이터를 가져올 때 사용하는 변수
    private List<todoList> tList = new ArrayList<>();
    private List<RepeatList> rList = new ArrayList<>();
    private List<String> tUid = new ArrayList<>();
    private List<String> rUid = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileupdate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profile = findViewById(R.id.CircleView);
        uEmail = findViewById(R.id.userEmail);
//        EditText uPassword = findViewById(R.id.userPassword);
//        EditText uPasswordCheck = findViewById(R.id.userPasswordCheck);
        uName = findViewById(R.id.userName);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // 프로필 사진을 클릭하면 앨범으로 이동
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAlbum();
            }
        });

        // 로그인한 사용자의 정보 가져오기
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String email = user.getEmail();
            String uid = user.getUid();

            uEmail.setText(email);

            database.getReference().child("profile_img").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userNameV = snapshot.child("userName").getValue().toString();
                        String profileImgUrlV = snapshot.child("profileImgUrl").getValue().toString();
                        Picasso.get().load(profileImgUrlV).into(profile);
                        uName.setText(userNameV);
                    } else if(user.getPhotoUrl()!=null){// 구글 로그인 상태라면
                            try {
                                URL url = new URL(user.getPhotoUrl().toString());
                                gProfileV = url.toString();
                                gNameV = user.getDisplayName();
                                Picasso.get().load(gProfileV).into(profile);
                                uName.setText(gNameV);
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

        dialog01 = new Dialog(ProfileUpdate.this);
        dialog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog01.setContentView(R.layout.dialog01);

        dialog02 = new Dialog(ProfileUpdate.this);
        dialog02.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog02.setContentView(R.layout.dialog02);

        // 수정 버튼을 클릭하면
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogUpdate();
            }
        });

        // 탈퇴 버튼을 클릭하면
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog01();
            }
        });

        // todoList에 들어있는 값들을 조회해서 List에 추가
        database.getReference().child("todoList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                tList.clear();
                tUid.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    todoList todolistDto = ds.getValue(todoList.class);
                    String uidKey = ds.getKey();

                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(todolistDto.getUid())) {
                        tList.add(todolistDto);
                        tUid.add(uidKey);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // todoList에 들어있는 값들을 조회해서 List에 추가
        database.getReference().child("repeatList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                rList.clear();
                rUid.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    RepeatList repeatListDto = ds.getValue(RepeatList.class);
                    String uidKey = ds.getKey();

                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(repeatListDto.getUid())) {
                        rList.add(repeatListDto);
                        rUid.add(uidKey);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }   // onCreate

    // 앨범에서 사진 고르기
    private void gotoAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        tempFile = null;
                    }
                }
            }
            return;
        }
        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                imgUri = data.getData();
                pathUri = getPath(data.getData());
                profile.setImageURI(imgUri); // 이미지 띄움
                break;
            }
        }
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

    // dialog01을 디자인하는 함수
    public void showDialog01(){
        dialog01.show();
        dialog01.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));  // 투명 배경

        // 아니오 버튼
        Button noBtn = dialog01.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog01.dismiss();  // 다이알로그 닫기
            }
        });

        // 네 버튼
        dialog01.findViewById(R.id.yesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();

                    for(int i=0; i<tList.size(); i++){
                        if(tList.size() != 0) {
                            database.getReference().child("todoList").child(tUid.get(i)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("error: "+e.getMessage());
                                }
                            });
                    }
                }

                    for(int i=0; i<rList.size(); i++){
                        if(rList.size() != 0){
                        database.getReference().child("repeatList").child(rUid.get(i)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("error: "+e.getMessage());
                            }
                        });
                    }
                }

                revokeAccess();

                    if(uid != null){
                        // profile_img(DB)의 값 지우기
                        databaseReference.child("profile_img").child(uid).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                // 값 지우는 것을 성공하면
                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d("deleteUser", "user account deleted");
                                            // 값을 삭제하기 전에 리스트 조회
                                            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                                            startActivity(intent);
                                            dialog01.dismiss();
                                        }
                                    }
                                });  // user 삭제
                            }
                        }); // profile_img 삭제
                    }

            }  // onclick
        });  // 네 버튼
    }  // showDialog01


    // 수정버튼을 클릭하면 나타나는 Dialog
    public void showDialogUpdate(){
        dialog02.show();
        dialog02.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));  // 투명 배경

        // 취소 버튼
        Button noBtnUpdate = dialog02.findViewById(R.id.noBtnUpdate);
        noBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog02.dismiss();  // 다이알로그 닫기
            }
        });

        // 확인 버튼
        Button yesBtnUpdate = dialog02.findViewById(R.id.yesBtnUpdate);
        yesBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                Uri file = Uri.fromFile(new File(pathUri)); // path

                // 사용자 이메일 수정
                user.updateEmail(uEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("email", "User email address updated"+user.getEmail());

                            database.getReference().child("profile_img").child(uid).child("userName").setValue(uName.getText().toString());

                            // 스토리지에 선택한 이미지 넣음
                            StorageReference storageRef = mStorage.getReference().child("profile_img").child("uid/"+file.getLastPathSegment());
                            storageRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    final Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                                    while (!imageUrl.isComplete()) ;

                                    // db에 저장
                                    database.getReference().child("profile_img").child(uid).child("profileImgUrl").setValue(imageUrl.getResult().toString());
                                }
                            });

                            signOut();  // 수정하고 나서 로그아웃 시킴.
                        }
                    }
                });  // 이메일 수정
            }
        });
    } // showDialogUpdate

    // 로그아웃
    private void signOut () {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.d("usertest :", user + "");
                    updateUI(user);

                    FirebaseAuth.getInstance().signOut();
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

    // 구글 회원 탈퇴
    private void revokeAccess(){
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("google : ", "구글 탈퇴");
                Intent intent = new Intent(ProfileUpdate.this, LogInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
