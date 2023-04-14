package com.example.doit;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

// 커스텀한 다이얼로그 띄우고 다이얼로그 안에서 동작 정의
public class CustomDialog extends Dialog {
    static CustomDialog customDialog;
    private  CustomDialog(@NonNull Context context){
        super(context);
    }
    public static CustomDialog getInstance(Context context) {
        customDialog = new CustomDialog(context);
        return customDialog;
    }

    //다이얼로그 생성
    public void showDialog() {
        // 참조할 다이얼로그 화면 연결
        customDialog.setContentView(R.layout.activity_profile);
        // 다이얼로그 동작
        ImageButton btn_x = customDialog.findViewById(R.id.btn_x);
        btn_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });


        customDialog.show();
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //투명배경
    }
}
