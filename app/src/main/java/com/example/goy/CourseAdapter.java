package com.example.goy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private List<Course> courseList;

    public CourseAdapter(List<Course> courseList){
        this.courseList = courseList;
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null){
                    int pos = viewHolder.getAdapterPosition();
                    onItemClickListener.onItemClick(pos);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(longClickListener != null){
                    int pos = viewHolder.getAdapterPosition();
                    longClickListener.onItemLongClick(pos);
                    return true;
                }
                return false;
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.courseTitle.setText(courseList.get(position).getGroup());
        holder.courseDays.setText(courseList.get(position).getDaysFlattened());
        holder.courseLocation.setText(courseList.get(position).getLocationsFlattened());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void deleteItem(int pos){
        courseList.remove(pos);
        notifyItemRemoved(pos);
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
