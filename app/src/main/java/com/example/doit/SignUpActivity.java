package com.example.doit;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private final int PICK_FROM_ALBUM = 10;
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextName;
    private CircleImageView profile2;
    private Button buttonJoin;
    private String pathUri;
    private File tempFile;
    private Uri imgUri;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // auth, Database, Storage 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPassword = (EditText) findViewById(R.id.editText_passWord);
        editTextName = (EditText) findViewById(R.id.editText_name);

        profile2 = findViewById(R.id.CircleView);
        profile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoAlbum();
            }
        });

        buttonJoin = (Button) findViewById(R.id.btn_join);
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

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
                profile2.setImageURI(imgUri); // 이미지 띄움
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

    // 회원가입
    private void signUp() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String pw = editTextPassword.getText().toString();

        // 프로필사진, 이름, 이메일, 비밀번호 중 하나라도 비어있으면 return
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pw) || profile2.getDrawable() == null) {
            Toast.makeText(getApplicationContext(), "정보를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Authentication에 email,pw 생성
            firebaseAuth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // uid에 task, 선택된 사진을 file에 할당
                        String uid = task.getResult().getUser().getUid();
                        Uri file = Uri.fromFile(new File(pathUri)); // path

                        // 스토리지에 선택한 이미지 넣음
                        StorageReference storageRef = mStorage.getReference().child("profile_img").child("uid/"+file.getLastPathSegment());
                        storageRef.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                final Task<Uri> imageUrl = task.getResult().getStorage().getDownloadUrl();
                                while (!imageUrl.isComplete()) ;

                                UserModel userModel = new UserModel();

                                userModel.userName = name;
                                userModel.uid = uid;
                                userModel.profileImgUrl = imageUrl.getResult().toString();

                                // db에 저장
                                mDatabase.getReference().child("profile_img").child(uid).setValue(userModel);
                            }
                        });

                        // 회원가입 성공시
                        Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // 계정이 중복된 경우
                        Log.d("task",task.getException().getMessage()+"");
                        Toast.makeText(SignUpActivity.this, "이미 존재하는 계정입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
//        startActivity(intent);
    }
}
