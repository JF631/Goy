package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DepartmentAdapter extends BaseAdapter{

    private List<Pair<Course, LocalDate>> courseList;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final HashMap<String, String> MY_MAP = new HashMap() {{
        put("MONDAY", "Montag");
        put("TUESDAY", "Dienstag");
        put("WEDNESDAY", "Mittwoch");
        put("THURSDAY", "Donnerstag");
        put("FRIDAY", "Freitag");
        put("SATURDAY", "Samstag");
        put("SUNDAY", "Sonntag");
    }};

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
        ViewHolder viewHolder = new ViewHolder(view);

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Course course = courseList.get(position).getFirst();
        LocalDate date = courseList.get(position).getSecond();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(viewHolder.ctx);
        String duration = dataBaseHelper.getDuration(course, date.getDayOfWeek());
        viewHolder.dateView.setText(date.format(formatter) + " (" + MY_MAP.get(date.getDayOfWeek().toString()) + ")");
        viewHolder.durationView.setText(duration);
        viewHolder.courseView.setText(course.getGroup());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void switchList(List<Pair<Course, LocalDate>> courseList){
        this.courseList = courseList;
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean deleteItem(int pos, Context ctx){
        LocalDate date = courseList.get(pos).getSecond();
        String course = courseList.get(pos).getFirst().getGroup();
        AtomicBoolean rtrn = new AtomicBoolean(false);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Datum löschen?")
                .setMessage("Möchten Sie den " + date.format(formatter) + " vom Kurs " + course + " löschen?")
                .setCancelable(false)
                .setPositiveButton("Löschen", (dialogInterface, i) -> {
                    if(!dataBaseHelper.deleteDate(courseList.get(pos).getFirst(), date)){
                        Toast.makeText(ctx, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                    }else {
                        courseList.remove(pos);
                        notifyItemRemoved(pos);
                        rtrn.set(true);
                    }
                    dialogInterface.dismiss();

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    notifyItemChanged(pos);
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
        return rtrn.get();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView, durationView, courseView;
        Context ctx;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
            durationView = itemView.findViewById(R.id.simple_duration_row);
            courseView = itemView.findViewById(R.id.simple_course_row);
            ctx = itemView.getContext();
        }
    }
}
