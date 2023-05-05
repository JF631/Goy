package com.example.goy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class FilesFragment extends Fragment {
    private ArrayList<File> files;
    private View emptyView;

    public FilesFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        File[] downloadDirs = requireContext().getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
        files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(downloadDirs[0].listFiles())));
        View view;
        emptyView = inflater.inflate(R.layout.empty_window, container, false);
        if (files.isEmpty()){
            view = emptyView;
        }else {
            view = inflater.inflate(R.layout.file_view, container, false);
            RecyclerView recyclerView = view.findViewById(R.id.file_recycler_view);
            FileAdapter fileAdapter = new FileAdapter(files);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(fileAdapter);
            fileAdapter.setOnItemLongClickListener(pos -> fileAdapter.deleteItem(pos, getContext()));

            SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(fileAdapter, requireContext());
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);

            fileAdapter.setOnItemClickListener(pos -> {
                File file = files.get(pos);
                MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Aktionen")
                        .setMessage("Möchten Sie die Datei " + file.getName() + " öffnen oder teilen?")
                        .setCancelable(false)
                        .setPositiveButton("Öffnen", (dialogInterface, i) -> {
                            showPdf(file);
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .setNeutralButton("Teilen", (dialogInterface, i) -> {
                            sharePdf(file);
                            dialogInterface.dismiss();
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            });
        }

        return view;
    }


    private void showPdf(File file){
        if(file.exists()){
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() +".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            }catch (ActivityNotFoundException e){
                Toast.makeText(requireContext(), "Keine App zum öffnen von Pdf Dateien gefunden!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sharePdf(File file){
        if(file.exists()){
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() +".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            requireContext().startActivity(Intent.createChooser(shareIntent, "Zettel senden"));
        }
    }
}
