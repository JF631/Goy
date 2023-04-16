package com.example.goy;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final CourseAdapter courseAdapter;
    private final Context ctx;


    public SwipeToDeleteCallback(CourseAdapter courseAdapter, Context ctx) {
        super(0, ItemTouchHelper.LEFT);
        this.courseAdapter = courseAdapter;
        this.ctx = ctx;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        courseAdapter.deleteItem(pos, ctx);

    }
}
