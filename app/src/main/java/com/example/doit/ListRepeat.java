package com.example.doit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ListRepeat extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    List<RepeatList> rlists = new ArrayList<>();
    List<String> uidList = new ArrayList<>();
    AlertDialog.Builder alt_bld;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repeatlist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewRepeat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        NavigationRepeatList adapter = new NavigationRepeatList(rlists, uidList);

        FloatingActionButton fab = findViewById(R.id.fab);
        EditText et = new EditText(getApplicationContext());

        Calendar cal = Calendar.getInstance();
        Date nowDate = cal.getTime();
        SimpleDateFormat dataformat = new SimpleDateFormat("yyyy-MM-dd");
        final String date = dataformat.format(nowDate);

        // EditText 길이가 너무 길다 => Margin 속성 필요
        FrameLayout container = new FrameLayout(ListRepeat.this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        et.setLayoutParams(params);
        container.addView(et);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et.setText("");
                alt_bld = new AlertDialog.Builder(ListRepeat.this, R.style.MyAlertDialogStyle);
                alt_bld.setTitle("반복 일정 추가");
                alt_bld.setMessage("반복할 일정을 입력하세요.");
                alt_bld.setIcon(R.drawable.note);
                if(et.getParent() != null){
                    ((ViewGroup)et.getParent()).removeView(et);
                }
                alt_bld.setView(et);
                alt_bld.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String value  = et.getText().toString();
                                String uid = user.getUid();
                                RepeatList repeatList = new RepeatList(value, 0, uid, date);
                                database.getReference().child("repeatList").push().setValue(repeatList);
                            }
                        });
                alt_bld.setNegativeButton("취소", null);
                alt_bld.show();
            }
        });

        database.getReference().child("repeatList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rlists.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    RepeatList repeatListDto = ds.getValue(RepeatList.class);
                    String uidKey = ds.getKey();

                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(repeatListDto.getUid())) {
                        rlists.add(repeatListDto);
                        uidList.add(uidKey);
                    }
                }
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
