package com.example.goy;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class DatabaseWorker extends Worker {
    private static final String TAG = "DatabaseWorker";


    public DatabaseWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getApplicationContext());
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("GoyPrefs", MODE_PRIVATE);
        int minBeforeCourse = sharedPreferences.getInt("minutes_before_course", 30);
        LocalDate date = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        List<Pair<LocalTime, LocalTime>> timeList = dataBaseHelper.getTimesForWeekday(date.getDayOfWeek());
        if(timeList == null){
            Log.d(TAG, "no times found for: " + date.getDayOfWeek().name());
            return Result.success();
        }
        Data inputData = getInputData();
        String location = inputData.getString("location");
        String enteringTime = inputData.getString("left");
        if(enteringTime != null){
            String[] fenceTime = enteringTime.replaceAll("[()]", "").split(",");
            LocalTime enteredAt = LocalTime.parse(fenceTime[1]);
            if(Duration.between(enteredAt, currentTime).toMinutes() < 30) return Result.success();
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
            return Result.success();
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
        return Result.success();
    }
}
