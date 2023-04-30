package com.example.goy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends BaseAdapter{
    private final ArrayList<File> files;

    public FileAdapter(ArrayList<File> files){
        this.files = files;
    }

    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
    private FileAdapter.OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(FileAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(int pos);
    }
    private FileAdapter.OnItemLongClickListener onItemLongClickListener;
    public void setOnItemLongClickListener(FileAdapter.OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
    }
    @Override
    public void deleteItem(int pos, Context ctx) {
        File file = files.get(pos);
        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Datei löschen?")
                .setMessage("Möchten Sie die Datei " + file.getName() + " löschen?")
                .setCancelable(false)
                .setPositiveButton("Löschen", (dialogInterface, i) -> {
                    if(!file.delete()){
                        Toast.makeText(ctx, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                    }else {
                        files.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    dialogInterface.dismiss();

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    notifyItemChanged(pos);
                });
        AlertDialog dialog = alertBuilder.create();
        dialog.show();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent,false);
        FileAdapter.ViewHolder viewHolder = new FileAdapter.ViewHolder(view);

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
        viewHolder.fileName.setText(files.get(position).getName());

    }

    @Override
    public int getItemCount() {return files.size();}

    private static class ViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name_row);
        }
    }
}
