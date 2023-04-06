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

    private List<Triple<String, LocalTime, LocalTime>> times;

    public TimeRange(List<Triple<String, LocalTime, LocalTime>> times){
        this.times = times;
    }

    public List<Triple<String, LocalTime, LocalTime>> getTimes(){
        return times;
    }

    @NonNull
    public String toString(){
        return times.toString();
    }
}
