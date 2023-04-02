package com.example.goy;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class Course {
    private String department, group;
    private long courseId = -1;
    private TimeRange range;
    public Course(String department, String group, TimeRange range){
        this.department = department;
        this.group = group;
        this.range = range;
    }

    public Course(String department, String group, TimeRange range, int id){
        this.department = department;
        this.group = group;
        this.range = range;
        this.courseId = id;
    }

    public String getDepartment(){return department;}
    public String getGroup(){return group;}
    public String getStartTime(){return range.getStart().toString();}
    public String getEndTime(){return range.getEnd().toString();}
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDuration(){return range.getDurationString();}
    public String getDays(){return range.flattenDays();}

    public long getId(){return courseId;}

    public String getStringId(){return Long.toString(courseId);}

    public void setId(long id){this.courseId = id;}

}
