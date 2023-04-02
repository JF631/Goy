package com.example.goy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.ViewHolder> {

    private List<String> weekdays;

    public WeekdayAdapter(List<String> weekdays){
        this.weekdays = weekdays;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekday_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekdayAdapter.ViewHolder holder, int position) {
        holder.checkedTextView.setText(weekdays.get(position));
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

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            checkedTextView = itemView.findViewById(R.id.create_weekday_item);
        }
    }
}
