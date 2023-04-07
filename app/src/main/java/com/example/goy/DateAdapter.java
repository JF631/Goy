package com.example.goy;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private final List<Date> dateList;

    public DateAdapter(List<Date> dateList){
        this.dateList = dateList;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    private CourseAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(CourseAdapter.OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int pos);
    }

    private CourseAdapter.OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(CourseAdapter.OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public DateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_date_item, parent, false);
        DateAdapter.ViewHolder viewHolder = new DateAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DateAdapter.ViewHolder holder, int position) {
        holder.dateView.setText(dateList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
        }
    }
}
