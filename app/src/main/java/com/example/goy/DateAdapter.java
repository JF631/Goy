package com.example.goy;


import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private final List<LocalDate> dateList;

    public DateAdapter(List<LocalDate> dateList){
        this.dateList = dateList;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
    private DateAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(DateAdapter.OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int pos);
    }

    public void setOnItemLongClickListener(DateAdapter.OnItemLongClickListener listener){
        this.longClickListener = listener;
    }
    private DateAdapter.OnItemLongClickListener longClickListener;

    @NonNull
    @Override
    public DateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_date_item, parent, false);
        DateAdapter.ViewHolder viewHolder = new DateAdapter.ViewHolder(view);

        view.setOnClickListener(view1 -> {
            if (onItemClickListener != null){
                int pos = viewHolder.getAdapterPosition();
                onItemClickListener.onItemClick(pos);
            }
        });

        view.setOnLongClickListener(view1 -> {
            if(longClickListener != null) {
                int pos = viewHolder.getAdapterPosition();
                longClickListener.onItemLongClick(pos);
                return true;
            }
            return false;
        });
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull DateAdapter.ViewHolder holder, int position) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        holder.dateView.setText(dateList.get(position).format(formatter));
    }

    @Override
    public int getItemCount() {return dateList.size();}

    public void deleteItem(int pos){
        dateList.remove(pos);
        notifyItemRemoved(pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertItem(LocalDate date){
        dateList.add(date);
        Collections.sort(dateList, Comparator.reverseOrder());

        int newPosition = dateList.indexOf(date);
        notifyItemInserted(newPosition);

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
        }
    }
}
