package com.example.goy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedules.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TUS_TABLE =
            "CREATE TABLE schedules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "department_name TEXT,"+
                    "group_name TEXT," +
                    "weekdays TEXT," +
                    "start_time TEXT,"+
                    "end_time TEXT," +
                    "duration TEXT" +
                    ")";

    private static final String CREATE_TABLE_COURSE_DATE =
            "CREATE TABLE course_date (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"  +
                    "courseId INTEGER, " +
                    "date TEXT, " +
                    "FOREIGN KEY(courseId) REFERENCES course(id))";

    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TUS_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE_COURSE_DATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS schedules");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS course_date");
        onCreate(sqLiteDatabase);
    }

    public int getCourseId(Course course){ //this method is unsafe!!
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"id"};
        String selection = "department_name = ? AND group_name = ? AND start_time = ? AND end_time = ?";
        String[] args = {course.getDepartment(), course.getGroup(), course.getStartTime(), course.getEndTime()};

        Cursor cursor = db.query("schedules", projection, selection, args, null, null, null);
        int rtrn = -1;
        if(cursor.moveToFirst()){
            rtrn = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();
        db.close();

        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Course> getCourses(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Course> courses = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM schedules", null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String department = cursor.getString(cursor.getColumnIndexOrThrow("department_name"));
            String group = cursor.getString(cursor.getColumnIndexOrThrow("group_name"));
            String startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
            String endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
            String days = cursor.getString(cursor.getColumnIndexOrThrow("weekdays"));

            TimeRange range = new TimeRange(startTime, endTime, days);
            Course course = new Course(department, group, range, id);
            courses.add(course);

        }

        return courses;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<DayOfWeek> getAllWeekdays(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<DayOfWeek> rtrn = new ArrayList<>();
        String[] projection = {"weekdays"};
        String selection = "";
        String[] args = {};

        Cursor cursor = db.query("schedules", projection, selection, args, null, null, null);

        while (cursor.moveToNext()){
            String weekdayString = cursor.getString(cursor.getColumnIndexOrThrow("weekdays"));
            if(weekdayString.contains(",")){
                String[] days = weekdayString.split(",");
                for(String day : days){
                    if(!rtrn.contains(DayOfWeek.valueOf(day))){
                        rtrn.add(DayOfWeek.valueOf(day));
                    }
                }
            } else {
                if(!rtrn.contains(DayOfWeek.valueOf(weekdayString))){
                    rtrn.add(DayOfWeek.valueOf(weekdayString));
                }
            }
        }
        cursor.close();
        db.close();

        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Pair<LocalTime, LocalTime> getTimes(DayOfWeek weekday){
        String start = null, end = null;
        String sqlQuery = "SELECT MIN(CAST(start_time AS TIME)) FROM schedules WHERE weekdays = " + weekday + ";";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(sqlQuery, null);
        if (cursor.moveToFirst()) {
            start = cursor.getString(0);
        }

        sqlQuery = "SELECT MAX(CAST(end_time AS TIME)) FROM schedules WHERE weekdays = " + weekday + "; " ;

        cursor = db.rawQuery(sqlQuery, null);
        if (cursor.moveToFirst()) {
            end = cursor.getString(0);
        }

        Pair<LocalTime, LocalTime> rtrn = new Pair<>(LocalTime.parse(start), LocalTime.parse(end));

        cursor.close();
        db.close();

        return rtrn;

    }

    private boolean dateExists(Course course, String date){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"date"};
        String selection = "courseId = ? AND date = ?";
        String[] args = {course.getStringId(), date};

        Cursor cursor = db.query("course_date", projection, selection, args, null, null, null);

        boolean rtrn = cursor.moveToFirst();
        db.close();
        cursor.close();
        return rtrn;
    }

    public boolean insertDate(Course course, String date){
        if(course.getId() == -1){
            Log.e("Course Failure: ", "couldn't get course id" );
            return false;
        }
        if(dateExists(course, date)){return false;}
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("courseId", course.getId());
        values.put("date", date);
        db.insert("course_date", null, values);
        db.close();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<LocalDate> getDates(Course course){
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        SQLiteDatabase db = this.getReadableDatabase();
        List<LocalDate> rtrn = new ArrayList<>();
        String[] projection = {"date"};
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};

        Cursor cursor = db.query("course_date", projection, selection, args, null, null, null);

        while (cursor.moveToNext()){
            String dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            rtrn.add(LocalDate.parse(dateString, dateFormat));
        }
        cursor.close();
        db.close();

        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertCourse(Course course){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("department_name", course.getDepartment());
        contentValues.put("group_name", course.getGroup());
        contentValues.put("start_time", course.getStartTime());
        contentValues.put("end_time", course.getEndTime());
        contentValues.put("duration", course.getDuration());
        contentValues.put("weekdays", course.getDays());
        long rtrn = db.insert("schedules", null, contentValues);
        return rtrn;
    }

    public boolean deleteCourse(Course course){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("schedules", "id = ?", new String[]{course.getStringId()});
            db.delete("course_date", "courseId = ?", new String[]{course.getStringId()});
        }catch (SQLException e){
            Log.e("sql deletion error: ", e.toString());
            db.close();
            return false;
        }
        db.close();
        return true;
    }
}
