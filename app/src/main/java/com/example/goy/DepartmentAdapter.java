package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder>{

    private List<Pair<Course, LocalDate>> courseList;

    public DepartmentAdapter(List<Pair<Course, LocalDate>> courseList){
        this.courseList = courseList;
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
    private DepartmentAdapter.OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(DepartmentAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int pos);
    }
    private DepartmentAdapter.OnItemLongClickListener onItemLongClickListener;
    public void setOnItemLongClickListener(DepartmentAdapter.OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_date_item, parent,false);
        ViewHolder viewHolder = new DepartmentAdapter.ViewHolder(view);

        view.setOnClickListener(view1 -> {
           if(onItemClickListener != null){
               int pos = viewHolder.getAdapterPosition();
               onItemClickListener.onItemClick(pos);
           }
        });

        view.setOnLongClickListener(view1 -> {
            if(onItemLongClickListener != null){
                int pos = viewHolder.getAdapterPosition();
                onItemLongClickListener.onItemLongClick(pos);
                return true;
            }
            return false;
        });

        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        holder.dateView.setText(courseList.get(position).getSecond().format(formatter));
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void switchDepartment(List<Pair<Course, LocalDate>> courseList){
        this.courseList = courseList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
        }
    }
}
