package com.example.goy;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Course implements Parcelable {
    private String department;
    private String group;
    private static final HashMap<String, String> MY_MAP = new HashMap() {{
        put("MONDAY", "Montag");
        put("TUESDAY", "Dienstag");
        put("WEDNESDAY", "Mittwoch");
        put("THURSDAY", "Donnerstag");
        put("FRIDAY", "Freitag");
        put("SATURDAY", "Samstag");
        put("SUNDAY", "Sonntag");
    }};
    private long courseId = -1;
    private List<Triple<String, LocalTime, LocalTime>> courseTimes;
    private Set<String> locations;
    public Course(String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes, Set<String> locations){
        this.department = department;
        this.group = group;
        this.courseTimes = courseTimes;
        this.locations = locations;
    }

    public Course(String department, String group, List<Triple<String, LocalTime, LocalTime>> courseTimes, int id, Set<String> locations){
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

    public static String getDepartment(@Nullable Course course){
        return course != null ? course.getDepartment() : null;
    }

    public static String getGroup(@Nullable Course course){
        return course != null ? course.getGroup() : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public HashMap<String, Pair<LocalTime, LocalTime>> getCourseTimesMap(){
        if(courseTimes == null) return null;
        return courseTimes.stream()
                .sorted(Comparator.comparing(Triple::getFirst, Comparator.reverseOrder()))
                .collect(Collectors.toMap(Triple::getFirst,
                t -> new Pair<>(t.getSecond(), t.getThird()),
                (oldVal, newVal) -> oldVal,
                HashMap::new
        ));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static HashMap<String, Pair<LocalTime, LocalTime>> getCourseTimesMap(Course course){
        return course != null ? course.getCourseTimesMap() : null;
    }

    public String getDaysFlattened(){
        List<String> days = new ArrayList<>();
        for(Triple<String, LocalTime, LocalTime> courseTime : courseTimes){
            days.add(MY_MAP.get(courseTime.getFirst()));
        }
        return TextUtils.join(",", days);
    }

    public Set<String> getLocations(){
        return locations != null ? locations : null;
    }

    public static Set<String> getLocations(@Nullable Course course){
        return course != null ? course.getLocations() : new HashSet<>();
    }

    public String getLocationsFlattened(){
        if(locations == null) return null;
        Log.d("COURSE: ", locations.toString());
        return String.join(",", locations);
    }

    public void setLocations(Set<String> locations){
        this.locations = locations;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setCourseTimes(HashMap<String, Pair<LocalTime, LocalTime>> times){
        courseTimes = times.entrySet()
                .stream()
                .map(entry -> new Triple<>(entry.getKey(), entry.getValue().getFirst(), entry.getValue().getSecond()))
                .collect(Collectors.toList());
    }
    public void setCourseTimes(List<Triple<String, LocalTime, LocalTime>> times){
        courseTimes = times;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public void setGroup(String group){
        this.group = group;
    }

    public long getId(){return courseId;}

    public String getStringId(){return Long.toString(courseId);}

    public void setId(long id){this.courseId = id;}

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<LocalDate> getDates(Context ctx){
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        List<LocalDate> dateList = dbHelper.getDates(this, true);
        dbHelper.close();
        return dateList;
    }

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
        parcel.writeStringList(new ArrayList<>(locations));
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
        locations = new HashSet<>(in.createStringArrayList());
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
