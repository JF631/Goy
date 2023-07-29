package com.example.goy;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.constants.StandardFonts;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
public class FileHandler {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void export(Uri uri, List<Pair<Course, LocalDate>> courseDateList,
                              Context ctx, String department){
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyMMdd");

        double sumDuration = 0;
        for(Pair<Course, LocalDate> cl : courseDateList){
            sumDuration += Double.parseDouble(dataBaseHelper.getDuration(cl.getFirst(), cl.getSecond().getDayOfWeek()));
        }
        SharedPreferences sharedPreferences  = ctx.getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        String name = decryptString(sharedPreferences.getString("name", ""));
        String surname = decryptString(sharedPreferences.getString("surname", ""));
        String docName = surname + "_" + name + "_" + LocalDate.now().format(dateTimeFormatter) + "_" + department + ".pdf";
        int size = courseDateList.size();
        if (size > 43) {
            Toast.makeText(ctx, "Bitte kleineren Zeitraum auswählen!", Toast.LENGTH_SHORT).show();
            return;
        }
        File[] downloadDirs = ctx.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
        String downloadDir = downloadDirs[0].getAbsolutePath();

        File pdfFile = new File(downloadDir, docName);
        AtomicBoolean overwrite = new AtomicBoolean(true);

        if(pdfFile.exists()){
            double finalSumDuration = sumDuration;
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(ctx)
                    .setTitle("Datei existiert bereits")
                    .setMessage("Möchten Sie die Datei überschreiben?")
                    .setCancelable(false)
                    .setPositiveButton("überschreiben", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        try {
                            writeToPdf(pdfFile, uri, courseDateList, finalSumDuration, size,
                                    ctx, department);
                        } catch (IOException e) {
                            Toast.makeText(ctx, "Es ist ein Fehler beim exportieren aufgetreten", Toast.LENGTH_LONG).show();
                            Log.e("Export error: ", e.toString());
                        }
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        overwrite.set(false);
                    });
            AlertDialog dialog = alertBuilder.create();
            dialog.show();
        } else {
            try {
                writeToPdf(pdfFile, uri, courseDateList, sumDuration, size,
                        ctx, department);
            } catch (IOException e) {
                Toast.makeText(ctx, "Es ist ein Fehler beim exportieren aufgetreten", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static double getCurrentDurationSum(DataBaseHelper dataBaseHelper, List<LocalDate> dates, Course course){
        return dates.stream()
                .mapToDouble(date -> {
                    try{
                        return Double.parseDouble(dataBaseHelper.getDuration(course, date.getDayOfWeek()));
                    }catch (NumberFormatException e){
                        return 0.0;
                    }
                })
                .sum();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static double getCurrentDurationSum(DataBaseHelper dataBaseHelper, List<Pair<Course, LocalDate>> courseDateList) {
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

    public static void showSnackbar(View rootView, String message) {
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("Dismiss", v -> snackbar.dismiss());
        snackbar.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String decryptString(String toDecrypt){
        try {
            return Utilities.decryptToString(toDecrypt);
        } catch (Exception e) {
            Log.e("Create Person", String.valueOf(e));
            return toDecrypt;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void writeToPdf(File pdfFile, Uri uri,
                                   List<Pair<Course, LocalDate>> courseLocalDateList,
                                   double sumDuration, int size,
                                   Context ctx, String department) throws IOException {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DataBaseHelper dataBaseHelper = new DataBaseHelper(ctx);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        InputStream inputStream;
        try {
            inputStream = ctx.getContentResolver().openInputStream(uri);
        }catch (FileNotFoundException e){
            Log.e("FAIL EXCDPTION: ", e.toString());
            Toast.makeText(ctx, "Datei (blanko Stundenzettel) konnte nicht geöffnet werden", Toast.LENGTH_LONG).show();
            return;
        }
        assert inputStream != null;
        PdfReader reader = new PdfReader(inputStream);
        PdfWriter writer = new PdfWriter(pdfFile);
        PdfDocument pdfDoc = new PdfDocument(reader, writer);
        Log.d("test", "pdfdoc writer");
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        Map<String, PdfFormField> fields = form.getFormFields();

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

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
    }
}

