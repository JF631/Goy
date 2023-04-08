package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.channels.ClosedChannelException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Course implements Parcelable {
    private final String department, group;
    private long courseId = -1;
    private List<Triple<String, LocalTime, LocalTime>> courseTimes;
    private List<String> locations;
    public Course(String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes, List<String> locations){
        this.department = department;
        this.group = group;
        this.courseTimes = courseTimes;
        this.locations = locations;
    }

    public Course(String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes, int id, List<String> locations){
        this.department = department;
        this.group = group;
        this.courseTimes = courseTimes;
        this.courseId = id;
        this.locations = locations;
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
        if(locations == null) return null;
        return locations;
    }

    public String getLocationsFlattened(){
        if(locations == null) return null;
        Log.d("COURSE: ", locations.toString());
        return String.join(",", locations);
    }

    public void setLocations(List<String> locations){
        this.locations = locations;
    }

    public long getId(){return courseId;}

    public String getStringId(){return Long.toString(courseId);}

    public void setId(long id){this.courseId = id;}

    @NonNull
    public String toString(){
        return "(Abteilung: " + department + ", Gruppe: " + group + ", id: " + courseId + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        List<String> days = new ArrayList<>(), start = new ArrayList<>(), end = new ArrayList<>();
        for(Triple<String, LocalTime, LocalTime> courseTime : courseTimes){
            days.add(courseTime.getFirst());
            start.add(courseTime.getSecond().toString());
            end.add(courseTime.getThird().toString());
        }
        parcel.writeString(department);
        parcel.writeString(group);
        parcel.writeLong(courseId);
        parcel.writeStringList(locations);
        parcel.writeStringList(days);
        parcel.writeStringList(start);
        parcel.writeStringList(end);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected Course(Parcel in) {
        List<String> days, start, end;
        List<Triple<String, LocalTime, LocalTime>> times = new ArrayList<>();
        department = in.readString();
        group = in.readString();
        courseId = in.readLong();
        locations = in.createStringArrayList();
        days = in.createStringArrayList();
        start = in.createStringArrayList();
        end = in.createStringArrayList();
        for(int i=0; i<days.size(); ++i){
            times.add(new Triple<>(days.get(i), LocalTime.parse(start.get(i)), LocalTime.parse(end.get(i))));
        }
        courseTimes = times;
    }

    public static final Creator<Course> CREATOR = new Creator<>() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
