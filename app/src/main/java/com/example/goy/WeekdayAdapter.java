package com.example.goy;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.ViewHolder> {

    private final List<String> weekdays;

    public interface OnItemClickListener{
        void onItemClick(int position, View itemView);
    }

    private WeekdayAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(WeekdayAdapter.OnItemClickListener listener){
        this.onItemClickListener = listener;
    }

    public WeekdayAdapter(List<String> weekdays){
        this.weekdays = weekdays;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekday_item, parent, false);
        ViewHolder viewHolder = new WeekdayAdapter.ViewHolder(view);
        view.setOnClickListener(view1 -> {
            if (onItemClickListener != null){
                int pos = viewHolder.getAdapterPosition();
                onItemClickListener.onItemClick(pos, view);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeekdayAdapter.ViewHolder holder, int position) {
        holder.checkedTextView.setText(weekdays.get(position));
        holder.startTime.setOnClickListener(view -> getTime(view, holder.startTime));
        holder.endTime.setOnClickListener(view -> getTime(view, holder.endTime));
    }

    private void getTime(View view, TextView textView){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(), (timePicker, i, i1) -> textView.setText(String.format("%02d:%02d", i, i1)), hour, minute, true);

        timePickerDialog.show();

    }

    @Override
    public int getItemCount() {
        return weekdays.size();
    }

    public String getItem(int position){
        return weekdays.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CheckedTextView checkedTextView;
        TextView startTime, endTime;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            checkedTextView = itemView.findViewById(R.id.create_weekday_item);
            startTime = itemView.findViewById(R.id.create_start_item);
            endTime = itemView.findViewById(R.id.create_end_item);
        }
    }
}
