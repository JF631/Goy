package com.example.goy;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private final List<LocalDate> dateList;
    private final Course course;

    public DateAdapter(List<LocalDate> dateList, Course course){
        this.dateList = dateList;
        this.course = course;
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
        LocalDate date = dateList.get(position);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(holder.ctx);
        String duration = dataBaseHelper.getDuration(course, date.getDayOfWeek());
        holder.dateView.setText(date.format(formatter));
        holder.durationView.setText(duration);
    }

    @Override
    public int getItemCount() {return dateList.size();}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void deleteItem(int pos, Context ctx){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = dateList.get(pos);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx)
                .setTitle("Datum löschen?")
                .setMessage("Möchten Sie den " + date.format(formatter) + " aus der Liste entfernen?")
                .setCancelable(false)
                .setPositiveButton("Löschen", (dialogInterface, i) -> {
                    if(!dataBaseHelper.deleteDate(course, date.format(formatter))){
                        Toast.makeText(ctx, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                    }else {
                        dateList.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    dialogInterface.dismiss();

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertItem(LocalDate date){
        dateList.add(date);
        Collections.sort(dateList, Comparator.reverseOrder());

        int newPosition = dateList.indexOf(date);
        notifyItemInserted(newPosition);

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView, durationView;
        Context ctx;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
            durationView = itemView.findViewById(R.id.simple_duration_row);
            ctx = itemView.getContext();
        }
    }
}
