package com.example.goy;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Utilities {

    public static LocalDate tryParse(@Nullable String dateString){
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            return LocalDate.parse(dateString, formatter);
        }catch (DateTimeParseException | NullPointerException e){
            return null;
        }
    }
}
