package com.example.goy;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Utilities {

    public static LocalDate tryParseDate(@Nullable String dateString){
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        try {
            return LocalDate.parse(dateString, formatter);
        }catch (DateTimeParseException | NullPointerException e){
            return null;
        }
    }

    public static LocalTime tryParseTime(@Nullable String timeString){
        try{
            return LocalTime.parse(timeString);
        }catch (DateTimeException | NullPointerException e){
            return null;
        }
    }
}
