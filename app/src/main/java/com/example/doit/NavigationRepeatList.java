package com.example.doit;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NavigationRepeatList extends RecyclerView.Adapter<NavigationRepeatList.ViewHolder> {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();
    private List<RepeatList> rlists;
    List<String> uidKey;
    public NavigationRepeatList(List<RepeatList> rlists, List<String> uidKey){
        this.rlists = rlists;
        this.uidKey = uidKey;
    }
    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton btnDelete;
        private TextView tvRepeatContent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRepeatContent = itemView.findViewById(R.id.tvRepeatContent);
            btnDelete = itemView.findViewById(R.id.btnRepeatDelete);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        RepeatList repeatList = rlists.get(position);
        holder.tvRepeatContent.setText(repeatList.getContent());

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference().child("repeatList").child(uidKey.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(v.getContext(), "반복삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("error: "+e.getMessage());
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return rlists==null?0:rlists.size();
    }
}
