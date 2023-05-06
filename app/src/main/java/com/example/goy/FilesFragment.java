package com.example.goy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
    private FileAdapter fileAdapter;

    public FilesFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        File[] downloadDirs = requireContext().getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
        files = new ArrayList<>(Arrays.asList(Objects.requireNonNull(downloadDirs[0].listFiles())));
        View view;
        View emptyView = inflater.inflate(R.layout.empty_window, container, false);
        if (files.isEmpty()){
            view = emptyView;
        }else {
            view = inflater.inflate(R.layout.file_view, container, false);
            RecyclerView recyclerView = view.findViewById(R.id.file_recycler_view);
            fileAdapter = new FileAdapter(files, requireContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(fileAdapter);
            fileAdapter.setOnItemLongClickListener(pos -> {
                File file = files.get(pos);
                MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Datei Aktionen")
                        .setMessage("Ausgewählte Datei: " + file.getName())
                        .setCancelable(false)
                        .setPositiveButton("Löschen", (dialogInterface, i) -> {
                            fileAdapter.deleteItem(pos, requireContext());
                            dialogInterface.dismiss();

                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            fileAdapter.notifyItemChanged(pos);
                        })
                        .setNeutralButton("Umbenennen", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            showRenameDialog(file);
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            });

            SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(fileAdapter, requireContext());
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);


            fileAdapter.setOnItemClickListener(pos -> {
                File file = files.get(pos);
                showPdf(file);
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

    private void showRenameDialog(File file) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Datei umbenennen")
                .setMessage("Möchtest du die Datei " + file.getName() + " umbenennen?");

        // Create an EditText view to get the new file name from the user
        EditText editText = new EditText(requireContext());
        editText.setText(file.getName());
        editText.setSelection(0, file.getName().lastIndexOf("."));
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        builder.setView(editText);

        builder.setPositiveButton("Umbenennen", (dialog, which) -> {
            // Get the new file name from the EditText view
            String newName = editText.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Der Dateiname darf nicht leer sein", Toast.LENGTH_SHORT).show();
                return;
            }

            // Rename the file with the new name
            renameFile(file, newName);
        });

        builder.setNegativeButton("Abbrechen", null);

        builder.show();
    }


    private void renameFile(File file, String newName) {
        if (file.exists()) {
            File newFile = new File(file.getParentFile(), newName);
            if (file.renameTo(newFile)) {
                fileAdapter.updateItem(file, newFile);
            } else {
                // Failed to rename file
                Log.e("FileAdapter", "Failed to rename file: " + file.getAbsolutePath());
            }
        } else {
            // File does not exist
            Log.e("FileAdapter", "File does not exist: " + file.getAbsolutePath());
        }
    }

}
