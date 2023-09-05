package com.example.goy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@RequiresApi(api = Build.VERSION_CODES.O)
public class DepartmentFragment extends Fragment{
    private ActivityResultLauncher<String[]> documentPickerLauncher;
    List<Pair<Course, LocalDate>> courseDateList;
    private static DataBaseHelper dataBaseHelper;
    private DepartmentAdapter departmentAdapter;
    private String department;
    private boolean copyFile = false;
    private Comparator<Pair<Course, LocalDate>> byDate, byCourse, byDuration;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public DepartmentFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.department_date_view, container, false);
        String[] departments = getResources().getStringArray(R.array.departments);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        RecyclerView dateView = view.findViewById(R.id.department_dates_view);
        Spinner dpSpinner = view.findViewById(R.id.department_spinner);
        TextView startDate = view.findViewById(R.id.department_start_date);
        TextView endDate = view.findViewById(R.id.department_end_date);
        TextView dateSort = view.findViewById(R.id.date_sort);
        TextView durationSort = view.findViewById(R.id.duration_sort);
        TextView courseSort = view.findViewById(R.id.course_sort);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);

        dpSpinner.setAdapter(spinnerAdapter);
        ExtendedFloatingActionButton floatingActionButton = view.findViewById(R.id.export_btn);
        department = dpSpinner.getSelectedItem().toString();
        AtomicReference<String> start = new AtomicReference<>(), end = new AtomicReference<>();
        byDate = Comparator.comparing(Pair::getSecond);
        byCourse = Comparator.comparing(t->t.getFirst().getGroup());
        byDuration = Comparator.comparing(t -> dataBaseHelper.getDuration(t.getFirst(), t.getSecond().getDayOfWeek()));
        if (start.get() == null) startDate.setText("start");
        else startDate.setText(start.toString());
        if(end.get() == null)endDate.setText("end");
        else endDate.setText(end.toString());

        dataBaseHelper = new DataBaseHelper(getContext());
        courseDateList = dataBaseHelper.getDates(department, Utilities.tryParseDate(start.get()), Utilities.tryParseDate(end.get()));
        courseDateList.sort(byDate.reversed());
        departmentAdapter = new DepartmentAdapter(courseDateList);
        dateView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dateView.setAdapter(departmentAdapter);

        durationSort.setText("duration\n(\u2211" + FileHandler.getCurrentDurationSum(dataBaseHelper, courseDateList) + ")");

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(departmentAdapter, requireContext());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(dateView);

        final boolean[] isDesc = {true};

        startDate.setOnClickListener(view1 -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            MaterialDatePicker<Long> materialDatePicker = builder.build();

            materialDatePicker.addOnPositiveButtonClickListener(selectedDate -> {
                LocalDate localDate = Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate();
                start.set(localDate.format(formatter));
                startDate.setText(start.toString());
                courseDateList = dataBaseHelper.getDates(department, Utilities.tryParseDate(start.get()), Utilities.tryParseDate(end.get()));
                updateList(courseDateList, byDate ,isDesc[0]);
                durationSort.setText("duration\n(\u2211" + FileHandler.getCurrentDurationSum(dataBaseHelper, courseDateList) + ")");
            });

            materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        endDate.setOnClickListener(view1 -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            MaterialDatePicker<Long> materialDatePicker = builder.build();

            materialDatePicker.addOnPositiveButtonClickListener(selectedDate -> {
                LocalDate localDate = Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate();
                end.set(localDate.format(formatter));
                endDate.setText(end.toString());
                courseDateList = dataBaseHelper.getDates(department, Utilities.tryParseDate(start.get()), Utilities.tryParseDate(end.get()));
                updateList(courseDateList, byDate ,isDesc[0]);
                durationSort.setText("duration\n(\u2211" + FileHandler.getCurrentDurationSum(dataBaseHelper, courseDateList) + ")");
            });

            materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });

        Drawable descDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_down, null);
        assert descDrawable != null;
        descDrawable.setBounds(0, 0, descDrawable.getIntrinsicWidth(), descDrawable.getIntrinsicHeight());
        dateSort.setCompoundDrawables(descDrawable, null, null, null);
        dateSort.invalidate();

        Drawable ascDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_up, null);
        assert ascDrawable != null;
        ascDrawable.setBounds(0, 0, ascDrawable.getIntrinsicWidth(), ascDrawable.getIntrinsicHeight());
        dateSort.setOnClickListener(view1 -> {
            courseSort.setCompoundDrawables(null, null, null, null);
            durationSort.setCompoundDrawables(null, null, null, null);
            isDesc[0] = !isDesc[0];
            if (isDesc[0]) {
                dateSort.setCompoundDrawables(descDrawable, null, null, null);
            } else {
                dateSort.setCompoundDrawables(ascDrawable, null, null, null);
            }
            dateSort.invalidate();
            updateList(courseDateList, byDate, isDesc[0]);
        });

        durationSort.setOnClickListener(view1 -> {
            courseSort.setCompoundDrawables(null, null, null, null);
            dateSort.setCompoundDrawables(null, null, null, null);
            isDesc[0] = !isDesc[0];
            if (isDesc[0]) {
                durationSort.setCompoundDrawables(descDrawable, null, null, null);
            } else {
                durationSort.setCompoundDrawables(ascDrawable, null, null, null);
            }
            durationSort.invalidate();
            updateList(courseDateList, byDuration, isDesc[0]);

        });

        courseSort.setOnClickListener(view1 -> {
            dateSort.setCompoundDrawables(null, null, null, null);
            durationSort.setCompoundDrawables(null, null, null, null);
            isDesc[0] = !isDesc[0];
            if (isDesc[0]) {
                courseSort.setCompoundDrawables(descDrawable, null, null, null);
            } else {
                courseSort.setCompoundDrawables(ascDrawable, null, null, null);
            }
            courseSort.invalidate();
            updateList(courseDateList, byCourse, isDesc[0]);
        });

        documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                if(copyFile) {
                    try {
                        copyFile(uri, new File(requireContext().getExternalFilesDir(null), "TUS_Stundenzettel.pdf"));
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Kopieren fehlgeschlagen", Toast.LENGTH_LONG).show();
                    }
                }
                FileHandler.export(uri, courseDateList, requireContext(), department);
            }
        });

        floatingActionButton.setOnClickListener(view1 -> {
            MaterialAlertDialogBuilder exportDialog = new MaterialAlertDialogBuilder(requireContext());
            LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
            String currentStart = start.get() == null ? "Startdatum"
                    : start.get();
            String currentEnd = end.get() == null ? "Enddatum"
                    : end.get();
            View exportView = dialogInflater.inflate(R.layout.export_course_list_view, null);
            TextView exportStart = exportView.findViewById(R.id.export_start_date);
            TextView exportEnd = exportView.findViewById(R.id.export_end_date);
            TextView subtitle = exportView.findViewById(R.id.export_subtitle);
            subtitle.setText("Möchten Sie die Stunden für die Abteilung " + department + " exportieren?");
            MaterialSwitch exportAll = exportView.findViewById(R.id.export_all_groups);
            exportAll.setOnCheckedChangeListener((compoundButton, checked) -> {
                if(checked) {
                    Pair<LocalDate, LocalDate> startEnd = dataBaseHelper.getMinMaxDate();
                    subtitle.setText("Möchten Sie alle Stunden für folgenden Zeitraum exportieren?" + "\n" + "Bitte Zeitraum wählen:");
                    exportStart.setText(startEnd.getFirst().toString());
                    exportEnd.setText(startEnd.getSecond().toString());
                }
                else {
                    subtitle.setText("Möchten Sie die Stunden für die Abteilung " + department + " exportieren?");
                    exportStart.setText(currentStart);
                    exportEnd.setText(currentEnd);
                }
            });
            exportStart.setText(currentStart);
            exportEnd.setText(currentEnd);
            exportStart.setOnClickListener(exportStartView -> {
                MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                MaterialDatePicker<Long> materialDatePicker = builder.build();
                materialDatePicker.addOnPositiveButtonClickListener(selectedDate -> {
                    LocalDate localDate = Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate();
                    exportStart.setText(localDate.format(formatter));
                });
                materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            });
            exportEnd.setOnClickListener(exportStartView -> {
                MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                MaterialDatePicker<Long> materialDatePicker = builder.build();
                materialDatePicker.addOnPositiveButtonClickListener(selectedDate -> {
                    LocalDate localDate = Instant.ofEpochMilli(selectedDate).atZone(ZoneId.systemDefault()).toLocalDate();
                    exportEnd.setText(localDate.format(formatter));
                });
                materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            });
            exportDialog.setView(exportView)
                    .setTitle("Exportieren")
                    .setNegativeButton("Abbrechen", (dialog, which)-> dialog.dismiss())
                    .setPositiveButton("Exportieren", (dialog, which)-> {
                        if(sharedPreferences.getString("name", "").isEmpty()) showCreate();
                        File file = new File(requireContext().getExternalFilesDir(null), "TUS_Stundenzettel.pdf");
                        Log.d("FILE", file.getAbsolutePath());
                        if(file.exists()) {
                            Uri uri = Uri.fromFile(file);
                            List<Pair<Course, LocalDate>> dateList;
                            String tmpDepartment = department;
                            if(exportAll.isChecked())
                                tmpDepartment = "Alle";
                            dateList = dataBaseHelper.getDates(tmpDepartment, Utilities.tryParseDate(exportStart.getText().toString()),
                                        Utilities.tryParseDate(exportEnd.getText().toString()));
                            FileHandler.export(uri, dateList, requireContext(), tmpDepartment);
                        }else{
                            selectDocument();
                        }
                    });
            AlertDialog dialog = exportDialog.create();
            dialog.show();
        });


        dpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = adapterView.getItemAtPosition(i).toString();
                courseDateList = dataBaseHelper.getDates(department, Utilities.tryParseDate(start.get()), Utilities.tryParseDate(end.get()));
                updateList(courseDateList, byDate, isDesc[0]);
                durationSort.setText("duration\n(\u2211" + FileHandler.getCurrentDurationSum(dataBaseHelper, courseDateList) + ")");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        departmentAdapter.setOnItemClickListener(pos -> {
            CourseFragment courseFragment = new CourseFragment(courseDateList.get(pos).getSecond().toString());
            Bundle bundle = new Bundle();
            Course course = courseDateList.get(pos).getFirst();
            course.setCourseTimes(dataBaseHelper.getTimes(course));
            bundle.putParcelable("course", course);
            courseFragment.setArguments(bundle);
            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            courseFragment.setSharedElementEnterTransition(TransitionInflater.from(getContext())
                    .inflateTransition(android.R.transition.move));
            courseFragment.setEnterTransition(new Fade());
            fragmentTransaction.addSharedElement(floatingActionButton, "export_button");
            fragmentTransaction.replace(R.id.fragment_container_view, courseFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).hideNavBar();
        });

        departmentAdapter.setOnItemLongClickListener(pos -> departmentAdapter.deleteItem(pos, getContext()));

        return view;
    }

    private void copyFile(Uri source, File target) throws Exception {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(source)) {
            Files.copy(inputStream, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void showCreate(){
        CreatePersonFragment createPersonFragment = new CreatePersonFragment();
        createPersonFragment.show(getChildFragmentManager(), "create_person");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void selectDocument(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Leeren Stundenzettel auswählen")
                .setMessage("Falls du den Zettel nicht jedes mal neu auswählen möchtest, kopier ihn einfach in " + requireContext().getExternalFilesDir(null).getAbsolutePath())
                .setPositiveButton("Auswählen und kopieren", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    showDocumentPicker();
                    copyFile = true;

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> dialogInterface.dismiss())
                .setNeutralButton("Auswählen", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    showDocumentPicker();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDocumentPicker(){
        String mimeType = "application/pdf";

        // Create an intent to open the document chooser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);

        // Add the allowed directories as additional locations for the document chooser
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, requireActivity().getExternalFilesDir(null));


        // Start the document chooser activity
        documentPickerLauncher.launch(new String[]{mimeType});
    }

    private void updateList(List<Pair<Course, LocalDate>> values, Comparator<Pair<Course, LocalDate>> comp ,boolean desc){
        if(desc) values.sort(comp.reversed());
        else values.sort(comp);
        departmentAdapter.switchList(values);
    }

    private void sharePdf(File file){
        if(file.exists()){
            Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() +".provider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            requireContext().startActivity(Intent.createChooser(shareIntent, "Stundenzettel senden"));
        }
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
                Toast.makeText(requireContext(), "Keine App zum öffnnen von Pdf Dateien gefunden", Toast.LENGTH_LONG).show();
            }

        }
    }
}
