package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CourseAdapter extends BaseAdapter {

    private final List<Course> courseList;

    public CourseAdapter(List<Course> courseList){
        this.courseList = courseList;
    }

    public interface OnItemClickListener{
        void onItemClick(int position, View sharedView);
    }
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int pos);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
        CourseAdapter.ViewHolder viewHolder = new CourseAdapter.ViewHolder(view);

        view.setOnClickListener(view12 -> {
            if(onItemClickListener != null){
                int pos = viewHolder.getAdapterPosition();
                onItemClickListener.onItemClick(pos, view);
            }
        });

        view.setOnLongClickListener(view1 -> {
            if(longClickListener != null){
                int pos = viewHolder.getAdapterPosition();
                longClickListener.onItemLongClick(pos);
                return true;
            }
            return false;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.courseTitle.setText(courseList.get(position).getGroup());
        viewHolder.courseDays.setText(courseList.get(position).getDaysFlattened());
        viewHolder.courseLocation.setText(courseList.get(position).getLocationsFlattened());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean deleteItem(int pos, Context ctx){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        AtomicBoolean rtrn = new AtomicBoolean(false);
        Course course = courseList.get(pos);
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Kurs löschen?")
                .setMessage("Möchten Sie den Kurs " + course.getGroup() + " endgültig löschen?\n" +
                        "ACHTUNG: Standardmäßig werden auch alle zugehörigen Termine gelöscht!")
                .setCancelable(false)
                .setPositiveButton("Löschen", (dialogInterface, i) -> {
                    if(dataBaseHelper.deleteCourse(courseList.get(pos), true)){
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

    public void insertItem(Course course){
        int pos = getItemCount();
        courseList.add(course);
        notifyItemInserted(pos);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView courseTitle, courseDays, courseLocation;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            courseTitle = itemView.findViewById(R.id.course_title);
            courseDays = itemView.findViewById(R.id.course_days);
            courseLocation = itemView.findViewById(R.id.course_location);
        }
    }
}
