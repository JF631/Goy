package com.example.goy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.codec.Base64;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class DepartmentFragment extends Fragment{

    private static final String REQ_PERMISSIONS = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private ActivityResultLauncher<String[]> documentPickerLauncher;

    public DepartmentFragment(){}

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.department_date_view, container, false);
        RecyclerView dateView = view.findViewById(R.id.department_dates_view);
        Spinner dpSpinner = view.findViewById(R.id.department_spinner);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.export_btn);
        String department = dpSpinner.getSelectedItem().toString();
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());
        List<Pair<Course, LocalDate>> courseDateList = dataBaseHelper.getDates(department, null, null);
        documentPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri != null) {
                Log.d("DP", uri.getPath());
                export(uri, courseDateList);
                // Handle the selected file URI here
                // ...
            }
        });

        floatingActionButton.setOnClickListener(view1 -> {
            requestPermissions();

        });

        DepartmentAdapter departmentAdapter = new DepartmentAdapter(courseDateList);
        dateView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dateView.setAdapter(departmentAdapter);

        dpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString();
                List<Pair<Course, LocalDate>> newList = dataBaseHelper.getDates(selected, null, null);
                departmentAdapter.switchDepartment(newList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }


    private void export(Uri uri, List<Pair<Course,LocalDate>> courseLocalDateList){
        int size = courseLocalDateList.size();
        if (size >= 43){
            Toast.makeText(getContext(), "Bitte kleineren Zeitraum auswählen!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File pdfFile = new File(getContext().getExternalFilesDir(null), "test.pdf");
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputStream), new PdfWriter(pdfFile));
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            Map<String, PdfFormField> fields = form.getFormFields();
            int d = 0;
            String dateKey = "", durationKey = "";
            for(int i=0; i<43/*replace with size!!!*/; ++i){
                if(i % 22 == 0 && i != 0)  d = 1;
                dateKey = "dt1." + (i % 22) + "." + d;
                durationKey = "du1." + (i % 22) + "." + d;
                if (fields.containsKey(dateKey) && fields.containsKey(durationKey)){
                    Log.d("TEST", dateKey);
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestPermissions(){
        String mimeType = "application/pdf";

        // Create an intent to open the document chooser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(mimeType);

        // Add the allowed directories as additional locations for the document chooser
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getActivity().getExternalFilesDir(null));


        // Start the document chooser activity
        documentPickerLauncher.launch(new String[]{mimeType});
    }
}
