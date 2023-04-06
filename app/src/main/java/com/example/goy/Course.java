package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.channels.ClosedChannelException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Course {
    private String department, group;
    private Context ctx;
    private long courseId = -1;
    private List<Triple<String, LocalTime, LocalTime>> courseTimes;
    public Course(Context ctx, String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes){
        this.ctx = ctx;
        this.department = department;
        this.group = group;
        this.courseTimes = courseTimes;
    }

    public Course(String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes, int id){
        this.department = department;
        this.group = group;
        this.courseTimes = courseTimes;
        this.courseId = id;
    }

    public Course(String department, String group, int id){
        this.department = department;
        this.group = group;
        this.courseId = id;
    }

    public String getDepartment(){return department;}
    public String getGroup(){return group;}
    public List<Triple<String, LocalTime, LocalTime>> getCourseTimes(){
        if(courseTimes == null) return null;
        return courseTimes;
    }

    public String getDaysFlattened(){
        List<String> days = new ArrayList<>();
        for(Triple<String, LocalTime, LocalTime> courseTime : courseTimes){
            days.add(courseTime.getFirst());
        }
        return TextUtils.join(",", days);
    }

    public List<String> getLocations(){
        if(ctx == null) return null;
        DataBaseHelper dataBaseHelper  = new DataBaseHelper(ctx);
       return dataBaseHelper.getLocations(this);
    }

    public String getLocationsFlattened(){
        if(ctx == null) return null;
        DataBaseHelper dataBaseHelper  = new DataBaseHelper(ctx);
        return TextUtils.join(",", dataBaseHelper.getLocations(this));
    }

    public long getId(){return courseId;}

    public String getStringId(){return Long.toString(courseId);}

    public void setId(long id){this.courseId = id;}

    @NonNull
    public String toString(){
        return "(Abteilung: " + department + ", Gruppe: " + group + ", id: " + Long.toString(courseId) + ")";
    }

}
