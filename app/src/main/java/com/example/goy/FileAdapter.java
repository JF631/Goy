package com.example.goy;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends BaseAdapter{
    private ArrayList<File> files;
    private final Context ctx;

    public FileAdapter(ArrayList<File> files, Context ctx){
        this.files = files;
        this.ctx = ctx;
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx)
                .setTitle("Datei löschen")
                .setMessage("Möchtest du die Datei '" + file.getName() + "' wirklich löschen?")
                .setPositiveButton("Ja", (dialogInterface, i) -> {
                    if(!file.delete()){
                        Toast.makeText(ctx, "Es ist ein Fehler aufgetreten", Toast.LENGTH_SHORT).show();
                    }else {
                        files.remove(pos);
                        notifyItemRemoved(pos);
                    }
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    notifyItemChanged(pos);
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void updateItem(File file, File newFile){
        int pos = files.indexOf(file);
        if(pos >= 0){
            files.set(pos, newFile);
            notifyItemChanged(pos);
        }
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
        viewHolder.shareBtn.setOnClickListener(view -> {
            File file = files.get(position);
            sharePdf(file);
        });

    }

    private void sharePdf(File file){
        if(file.exists()){
            Uri uri = FileProvider.getUriForFile(ctx, ctx.getPackageName() +".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            ctx.startActivity(Intent.createChooser(shareIntent, "Zettel senden"));
        }
    }

    @Override
    public int getItemCount() {return files.size();}

    private static class ViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;
        ImageView shareBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name_row);
            shareBtn = itemView.findViewById(R.id.share_icon);
        }
    }
}
