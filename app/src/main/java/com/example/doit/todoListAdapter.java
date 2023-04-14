package com.example.doit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class todoListAdapter extends RecyclerView.Adapter<todoListAdapter.MyViewHolder> {

    private List<todoList> todoLists;
    private List<String> uidKey;
    private todoListListener listener;
    private Activity activity;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();

    public todoListAdapter(List<todoList> listDto, List<String> uidKey, Activity activity) {
        todoLists = listDto;
        this.uidKey = uidKey;  // todoList에 들어가 있는 uid값을 가져오기 위해 사용
        this.activity = activity;
    }

    String todoList_key;

    // Main에 값을 넘겨주기 위해 리스너를 사용
    public void setListener(todoListListener listener){
        this.listener = listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private CheckBox chkContent;
        private ImageButton btnDelete;
        private ImageButton btnUpdte;
        private ImageButton btnDate;

        public MyViewHolder(@NonNull View itemView, final todoListListener listener) {
            super(itemView);
            chkContent = itemView.findViewById(R.id.chkContent);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdte = itemView.findViewById(R.id.btnUpdate);
            btnDate = itemView.findViewById(R.id.btnDate);

            // 수정버튼을 클릭했을 때
            btnUpdte.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String key = uidKey.get(position);
                    listener.onItemClick(position, key);
                    chkContent.setVisibility(View.GONE);
                    btnUpdte.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);
                    btnDate.setVisibility(View.GONE);
                }
            });
        }
    }

    // position에 대한 값(todolist)를 반환해줌.
    public todoList getItem(int position){
        todoList todolist = todoLists.get(position);
        return todolist;
    }

    @NonNull
    @Override
    public todoListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view, listener);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        todoList todoList = todoLists.get(position);
        holder.chkContent.setText(todoList.getContent());

        // 체크박스 이벤트(chkId가 1이면 계속 클릭된 상태로 만들어줌)
        holder.chkContent.setChecked(todoList.getChkId()==1);
        holder.chkContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // selectedPosition = holder.getAdapterPosition();
                    database.getReference().child("todoList").child(uidKey.get(position)).child("chkId").setValue(1);
                } else {
                    //selectedPosition = -1;
                    database.getReference().child("todoList").child(uidKey.get(position)).child("chkId").setValue(0);
                }


            }
        });

        // 할일 삭제 버튼을 클릭하면 -> 삭제 되게 만듦
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference().child("todoList").child(uidKey.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(v.getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("error: "+e.getMessage());
                        Toast.makeText(v.getContext(), "삭제실패", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 해당 item을 롱클릭했을 때 Dialog를 생성해줌.
        // 확인 버튼을 클릭하면 DB에 값이 추가되는 형식
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                // 날짜 기본값(클릭을 하지 않으면 오늘날짜가 들어감)
                Calendar cal = Calendar.getInstance();
                Date nowDate = cal.getTime();
                SimpleDateFormat dataformat = new SimpleDateFormat("yyyy-MM-dd");
                String dateNow = dataformat.format(nowDate);

                AlertDialog.Builder rdialog = new AlertDialog.Builder(activity);
                rdialog.setTitle("반복 할일 지정")
                        .setMessage("반복 할 일로 등록하시겠습니까?")
                        .setIcon(R.drawable.todoit_logo)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String key = uidKey.get(position);

                                if(todoList.getFlag() == 0){
                                    // 반복 일정 추가(DB)
                                    RepeatList repeatList = new RepeatList(todoList.getContent(), 0, todoList.getUid(), dateNow);
                                    databaseReference.child("repeatList").push().setValue(repeatList);
                                    databaseReference.child("todoList").child(key).child("flag").setValue(1);
                                    Toast.makeText(activity, "등록 되었습니다.", Toast.LENGTH_SHORT).show();

                                    database.getReference().child("todoList").child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println("error: "+e.getMessage());
                                        }
                                    });
                                } else {
                                    Toast.makeText(activity, "반복루틴으로 등록되어있습니다..", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).setNegativeButton("취소", null)
                        .show();
                return false;
            }
        });

        // 날짜 수정 버튼
        holder.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                todoList_key = uidKey.get(position);
                DatePickerDialog dialog = new DatePickerDialog(activity, listener1, 2023, 1, 9);
                dialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return todoLists==null?0:todoLists.size();
    }

    String date;
    private DatePickerDialog.OnDateSetListener listener1 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            // 날짜 계산
            if(month<9 && dayOfMonth<10)
                date = year + "-0" + (month + 1) + "-0" + dayOfMonth;
            else if(month>=9 && dayOfMonth<10)
                date = year + "-" + (month + 1) + "-0" + dayOfMonth;
            else if(month<9 && dayOfMonth>=10)
                date = year + "-0" + (month + 1) + "-" + dayOfMonth;
            else if(month>=9 && dayOfMonth>=10)
                date = year + "-" + (month + 1) + "-" + dayOfMonth;

            // 날짜 변경
            databaseReference.child("todoList").child(todoList_key).child("date").setValue(date);


        }
    };
}