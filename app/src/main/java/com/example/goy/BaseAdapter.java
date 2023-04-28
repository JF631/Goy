package com.example.goy;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public abstract void deleteItem(int pos, Context ctx);

    public interface OnItemClickListener {
        void onItemClick(int position, View sharedView);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface AdapterClickListener {
        void onItemClicked(int position, View view);

        void onItemLongClicked(int position);
    }

    protected AdapterClickListener adapterClickListener;

    public void setAdapterClickListener(AdapterClickListener adapterClickListener) {
        this.adapterClickListener = adapterClickListener;
    }
}
