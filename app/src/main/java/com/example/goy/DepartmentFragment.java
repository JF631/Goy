package com.example.goy;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@RequiresApi(api = Build.VERSION_CODES.O)
public class DepartmentFragment extends Fragment{
    private ActivityResultLauncher<String[]> documentPickerLauncher;
    List<Pair<Course, LocalDate>> courseDateList;
    private static DataBaseHelper dataBaseHelper;
    private boolean rememberFile = false;
    private DepartmentAdapter departmentAdapter;
    private String department;
    private Comparator<Pair<Course, LocalDate>> byDate, byCourse, byDuration;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String[] departments = {"Leichtathletik", "Turnen", "Fitness"};

    public DepartmentFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.department_date_view, container, false);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, departments);
        RecyclerView dateView = view.findViewById(R.id.department_dates_view);
        Spinner dpSpinner = view.findViewById(R.id.department_spinner);
        TextView startDate = view.findViewById(R.id.department_start_date);
        TextView endDate = view.findViewById(R.id.department_end_date);
        TextView dateSort = view.findViewById(R.id.date_sort);
        TextView durationSort = view.findViewById(R.id.duration_sort);
        TextView courseSort = view.findViewById(R.id.course_sort);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        dpSpinner.setAdapter(spinnerAdapter);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.export_btn);
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

        durationSort.setText("duration\n(\u2211" + getCurrentDurationSum(courseDateList) + ")");

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
                durationSort.setText("duration\n(\u2211" + getCurrentDurationSum(courseDateList) + ")");
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
                durationSort.setText("duration\n(\u2211" + getCurrentDurationSum(courseDateList) + ")");
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
                if(rememberFile)
                    editor.putString("fileUri", uri.toString());
                else
                    editor.putString("fileUri", "");

                editor.apply();
                export(uri, courseDateList, department);
            }
        });

        floatingActionButton.setOnClickListener(view1 -> {
            String msg = "Möchten Sie die angezeigten Daten für die Abteilung " + department + " exportieren?";
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Daten exportieren?")
                    .setMessage(msg)
                    .setPositiveButton("Exportieren", (dialogInterface, i) -> {
                        if(sharedPreferences.getString("name", "").isEmpty()) showCreate();
                        if(sharedPreferences.getString("fileUri", "").isEmpty()) selectDocument();
                        else export(Uri.parse(sharedPreferences.getString("fileUri", "")), courseDateList, department);
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> dialogInterface.dismiss());


            if (sharedPreferences.getString("fileUri", "").length() > 0) {
                builder.setNeutralButton("Datei auswählen", (dialogInterface, i) -> selectDocument());
            }

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });


        dpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = adapterView.getItemAtPosition(i).toString();
                courseDateList = dataBaseHelper.getDates(department, Utilities.tryParseDate(start.get()), Utilities.tryParseDate(end.get()));
                updateList(courseDateList, byDate, isDesc[0]);
                durationSort.setText("duration\n(\u2211" + getCurrentDurationSum(courseDateList) + ")");
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void export(Uri uri, List<Pair<Course,LocalDate>> courseLocalDateList, String department) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyMMdd");
        double sumDuration = 0;
        for(Pair<Course, LocalDate> cl : courseLocalDateList){
            sumDuration += Double.parseDouble(dataBaseHelper.getDuration(cl.getFirst(), cl.getSecond().getDayOfWeek()));
        }
        SharedPreferences sharedPreferences  = requireContext().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        String name = decryptString(sharedPreferences.getString("name", ""));
        String surname = decryptString(sharedPreferences.getString("surname", ""));
        String docName = surname + "_" + name + "_" + LocalDate.now().format(dateTimeFormatter) + "_" + department + ".pdf";
        int size = courseLocalDateList.size();
        if (size >= 43) {
            Toast.makeText(getContext(), "Bitte kleineren Zeitraum auswählen!", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] downloadDirs = requireContext().getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
        String downloadDir = downloadDirs[0].getAbsolutePath();

        File pdfFile = new File(downloadDir, docName);
        AtomicBoolean overwrite = new AtomicBoolean(true);

        if(pdfFile.exists()){
            double finalSumDuration = sumDuration;
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Datei existiert bereits")
                    .setMessage("Möchten Sie die Datei überschreiben?")
                    .setCancelable(false)
                    .setPositiveButton("überschreiben", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        try {
                            writeToPdf(pdfFile, uri, courseLocalDateList, finalSumDuration, size);
                        } catch (IOException e) {
                            Toast.makeText(requireContext(), "Es ist ein Fehler beim exportieren aufgetreten", Toast.LENGTH_LONG).show();
                            Log.e("Export error: ", e.toString());
                        }
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        overwrite.set(false);
                    });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        }else {
            try {
                writeToPdf(pdfFile, uri, courseLocalDateList, sumDuration, size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private double getCurrentDurationSum(List<Pair<Course, LocalDate>> courseDateList) {
        return courseDateList.stream()
                .mapToDouble(pair -> {
                    try {
                        return Double.parseDouble(dataBaseHelper.getDuration(pair.getFirst(), pair.getSecond().getDayOfWeek()));
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }

    private void showCreate(){
        CreatePersonFragment createPersonFragment = new CreatePersonFragment();
        createPersonFragment.show(getChildFragmentManager(), "create_person");
    }

    private void writeToPdf(File pdfFile, Uri uri, List<Pair<Course, LocalDate>> courseLocalDateList, double sumDuration, int size) throws IOException {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        InputStream inputStream;
        try {
             inputStream = requireContext().getContentResolver().openInputStream(uri);
        }catch (FileNotFoundException e){
            Toast.makeText(requireContext(), "Datei (blanko Stundenzettel) konnte nicht geöffnet werden", Toast.LENGTH_LONG).show();
            return;
        }
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(pdfFile));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        Map<String, PdfFormField> fields = form.getFormFields();

        PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);

        for (Map.Entry<String, PdfFormField> entry : fields.entrySet()) {
            PdfFormField field = entry.getValue();
            field.setFont(font);
            field.setFontSize(10f); // Set the font size to 12
        }

        String name = sharedPreferences.getString("name","").isEmpty() ? "" : decryptString(sharedPreferences.getString("name",""));
        String surname = sharedPreferences.getString("surname", "").isEmpty() ? "" : decryptString(sharedPreferences.getString("surname", ""));
        String iban = sharedPreferences.getString("iban", "").isEmpty() ? "" : decryptString(sharedPreferences.getString("iban", ""));
        String bic = sharedPreferences.getString("bic", "").isEmpty() ? "" : decryptString(sharedPreferences.getString("bic", ""));
        String bank = sharedPreferences.getString("bank", "").isEmpty() ? "" : decryptString(sharedPreferences.getString("bank", ""));

        Objects.requireNonNull(fields.get("date")).setValue(LocalDate.now().format(dateTimeFormatter));
        Objects.requireNonNull(fields.get("prename")).setValue(name);
        Objects.requireNonNull(fields.get("name")).setValue(surname);
        Objects.requireNonNull(fields.get("iban")).setValue(iban);
        Objects.requireNonNull(fields.get("bic")).setValue(bic);
        Objects.requireNonNull(fields.get("bank")).setValue(bank);
        Objects.requireNonNull(fields.get("department")).setValue(department);
        Objects.requireNonNull(fields.get("sum")).setValue(decimalFormat.format(sumDuration));

        int d = 0;
        String dateKey, durationKey;
        for (int i = 0; i < size; ++i) {
            if (i % 22 == 0 && i != 0) d = 1;
            dateKey = "dt1." + (i % 22) + "." + d;
            durationKey = "du1." + (i % 22) + "." + d;
            if (fields.containsKey(dateKey) && fields.containsKey(durationKey)) {
                Log.d("TEST", "save");
                LocalDate localDate = courseLocalDateList.get(i).getSecond();
                String duration = dataBaseHelper.getDuration(courseLocalDateList.get(i).getFirst(), localDate.getDayOfWeek());
                Objects.requireNonNull(fields.get(dateKey)).setValue(localDate.format(dateTimeFormatter));
                Objects.requireNonNull(fields.get(durationKey)).setValue(duration);
            }
        }
        pdfDoc.close();
        Snackbar.make(requireActivity().findViewById(android.R.id.content), "Stundenzettel erstellt", Snackbar.LENGTH_LONG)
                .setAction("Anzeigen", view -> showPdf(pdfFile))
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void selectDocument(){
        final String[] items = {"Datei für den nächsten Export merken"};
        AtomicInteger selected = new AtomicInteger(-1);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Leeren Stundenzettel auswählen")
                .setPositiveButton("auswählen", (dialogInterface, i) -> {
                    Log.d("EXPORT: ", String.valueOf(selected.get()));
                    String mimeType = "application/pdf";
                    if(selected.get() == 0)
                        rememberFile = true;
                    // Create an intent to open the document chooser
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType(mimeType);

                    // Add the allowed directories as additional locations for the document chooser
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, requireActivity().getExternalFilesDir(null));


                    // Start the document chooser activity
                    documentPickerLauncher.launch(new String[]{mimeType});

                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> dialogInterface.dismiss())
                .setSingleChoiceItems(items, selected.get(), (dialogInterface, i) -> selected.set(i));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String decryptString(String toDecrypt){
        try {
            return Utilities.decryptToString(toDecrypt);
        } catch (Exception e) {
            Log.e("Create Person", String.valueOf(e));
            return toDecrypt;
        }
    }
}
