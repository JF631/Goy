package com.example.goy;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DateAdapter extends BaseAdapter {
    private final List<LocalDate> dateList;
    private final TextView courseTitle;
    private int highlightedPos = -1;
    private final Course course;

    private static final HashMap<String, String> MY_MAP = new HashMap() {{
        put("MONDAY", "Montag");
        put("TUESDAY", "Dienstag");
        put("WEDNESDAY", "Mittwoch");
        put("THURSDAY", "Donnerstag");
        put("FRIDAY", "Freitag");
        put("SATURDAY", "Samstag");
        put("SUNDAY", "Sonntag");
    }};

    public DateAdapter(List<LocalDate> dateList, Course course, TextView courseTitle){
        this.dateList = dateList;
        this.course = course;
        this.courseTitle = courseTitle;

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate date = dateList.get(position);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(viewHolder.ctx);
        String duration = dataBaseHelper.getDuration(course, date.getDayOfWeek());
        viewHolder.dateView.setText(date.format(formatter) + " (" + MY_MAP.get(date.getDayOfWeek().toString()) + ")");
        viewHolder.durationView.setText(duration);

        viewHolder.setHighlight(position == highlightedPos);
    }

    @Override
    public int getItemCount() {return dateList.size();}

    private void updateTitle(String msg){
        courseTitle.setText(msg);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean deleteItem(int pos, Context ctx){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate date = dateList.get(pos);
        AtomicBoolean rtrn = new AtomicBoolean(false);
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Datum löschen?")
                .setMessage("Möchten Sie den " + date.format(formatter) + " aus der Liste entfernen?")
                .setCancelable(false)
                .setPositiveButton("Löschen", (dialogInterface, i) -> {
                    if(!dataBaseHelper.deleteDate(course, date)){
                        Toast.makeText(ctx, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                    }else {
                        dateList.remove(pos);
                        notifyItemRemoved(pos);
                        String msg = "Kurs: " + course.getGroup() +
                                "\nBisher gehaltene Stundenzahl: " +
                                course.getTotalTime(ctx, null, null) +
                                "\nTermine: \n";
                        updateTitle(msg);
                        rtrn.set(true);
                    }
                    dialogInterface.dismiss();

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    highlightedPos = -1;
                    notifyItemChanged(pos);
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
        return rtrn.get();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertItem(LocalDate date){
        dateList.add(date);
        Collections.sort(dateList, Comparator.reverseOrder());

        int newPosition = dateList.indexOf(date);
        notifyItemInserted(newPosition);

    }

    public int highlightRow(String content){
        int pos = findPosByContent(content);
        if(pos >= 0){
            highlightedPos = pos;
            notifyItemChanged(pos);
        }
        return pos;
    }

    private int findPosByContent(String content){
        for(int i=0; i< dateList.size(); ++i){
            LocalDate localDate = dateList.get(i);
            if(localDate.toString().equals(content)){
                return i;
            }
        }
        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView dateView, durationView;
        Context ctx;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.simple_date_row);
            durationView = itemView.findViewById(R.id.simple_duration_row);
            ctx = itemView.getContext();
        }

        public void setHighlight(boolean highlight) {
            if (highlight) {
                final int FLASH_INTERVAL_MS = 200; // flash interval in milliseconds
                final int NUM_FLASHES = 3; // number of times to flash
                final Handler handler = new Handler();
                final Runnable flashRunnable = new Runnable() {
                    boolean isHighlighted = true;
                    int numFlashes = 0; // number of times flashed

                    @Override
                    public void run() {
                        if (numFlashes < NUM_FLASHES) {
                            // create ripple effect
                            int rippleColor = ContextCompat.getColor(itemView.getContext(), R.color.ripple_color);
                            RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{{}}, new int[]{rippleColor}), null, null);
                            itemView.setBackground(rippleDrawable);

                            // toggle highlight color
                            if (isHighlighted) {
                                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.ripple_color));
                            } else {
                                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                            }
                            isHighlighted = !isHighlighted;
                            numFlashes++;

                            // schedule next flash
                            handler.postDelayed(this, FLASH_INTERVAL_MS);
                        } else {
                            // reset background color
                            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                            itemView.setBackground(null);
                        }
                    }
                };
                handler.postDelayed(flashRunnable, FLASH_INTERVAL_MS);
            } else {
                // reset background color
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                itemView.setBackground(null);
            }
        }





    }
}
