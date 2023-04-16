package com.example.goy;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class IntentDatabaseService extends IntentService {
    private static final String TAG = "IntentDatabaseService";

    public IntentDatabaseService(){
        super("insertDateService");
    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        SharedPreferences sharedPreferences = this.getSharedPreferences("GoyPrefs", MODE_PRIVATE);
        int minBeforeCourse = sharedPreferences.getInt("minutes_before_course", 30);
        LocalDate date = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<Pair<LocalTime, LocalTime>> timeList = dataBaseHelper.getTimesForWeekday(date.getDayOfWeek());
        if(timeList == null){
            Log.d(TAG, "no times found for: " + date.getDayOfWeek().name());
            return;
        }
        assert intent != null;
        String location = intent.getStringExtra("location");
        String enteringTime = intent.getStringExtra("left");
        if(enteringTime != null){
            String[] fenceTime = enteringTime.replaceAll("[()]", "").split(",");
            LocalTime enteredAt = LocalTime.parse(fenceTime[1]);
            if(Duration.between(enteredAt, currentTime).toMinutes() < 30) return;
            for(Pair<LocalTime, LocalTime> time : timeList) {
                if (location.equals(fenceTime[0])) {
                    if(currentTime.isAfter(time.getSecond())){
                        Course course = dataBaseHelper.getCourse(date.getDayOfWeek(), time);
                        if(dataBaseHelper.getLocations(course).contains(location)){
                            if (!dataBaseHelper.insertDate(course, date)){
                                Log.d(TAG, "location couldn't be inserted");
                            }
                        }
                    }
                }
            }
            return;
        }

        for(Pair<LocalTime, LocalTime> time : timeList){
            if(currentTime.isAfter(time.getFirst().minusMinutes(minBeforeCourse)) && currentTime.isBefore(time.getSecond())){
                Course course = dataBaseHelper.getCourse(date.getDayOfWeek(), time);
                Log.d(TAG, "course id: "  + course.getStringId());
                if(dataBaseHelper.getLocations(course).contains(location)){
                    if(!dataBaseHelper.insertDate(course, date)) {
                        Log.d(TAG, "location couldn't be inserted");
                    }
                }else {
                    Log.d(TAG, "wrong location for course: " + location + "looking for: " + dataBaseHelper.getLocations(course).toString());
                }
            }else {
                Log.d(TAG, "was here when no course took place");
            }
        }
    }
}
