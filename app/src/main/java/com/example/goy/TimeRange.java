package com.example.goy;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TimeRange {
    private LocalTime start, end;
    private List<String> days;

    public TimeRange(LocalTime start, LocalTime end, List<String> days){
        this.start = start;
        this.end = end;
        this.days = days;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public TimeRange(String start, String end, String dayString){
        this.start = LocalTime.parse(start);
        this.end = LocalTime.parse(end);
        this.days = new ArrayList<String>(Arrays.asList(dayString.split(",")));
    }

    public LocalTime getStart(){
        return start;
    }

    public LocalTime getEnd(){
        return end;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long getDurationInMinutes(){
        return Duration.between(start, end).toMinutes();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDurationString(){
        return String.valueOf(this.getDurationInMinutes());
    }

    public List<String> getDays(){
        return days;
    }

    public String flattenDays(){
        return TextUtils.join(",", days);
    }

    @NonNull
    public String toString(){
        return start + " - " + end + " on " + days;
    }
}
