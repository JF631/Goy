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
import java.time.Duration;
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
    private static final String CREATE_COURSE_TABLE =
            "CREATE TABLE courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                    "department_name TEXT,"+
                    "group_name TEXT" +
                    ")";

    private static final String CREATE_DATE_TABLE =
            "CREATE TABLE course_date (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT,"  +
                    "courseId INTEGER, " +
                    "date TEXT, " +
                    "FOREIGN KEY(courseId) REFERENCES course(id))";

    private static final String CREATE_TIME_TABLE =
            "CREATE TABLE course_times (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "courseId INTEGER," +
                    "weekday TEXT," +
                    "startTime TEXT," +
                    "endTime TEXT," +
                    "duration TEXT," +
                    "FOREIGN KEY(courseId) REFERENCES course(id))";

    private static final String CREATE_LOCATION_TABLE =
            "CREATE TABLE course_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "courseId INTEGER," +
                    "location TEXT," +
                    "FOREIGN KEY(courseId) REFERENCES course(id))";


    public DataBaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_COURSE_TABLE);
        sqLiteDatabase.execSQL(CREATE_DATE_TABLE);
        sqLiteDatabase.execSQL(CREATE_TIME_TABLE);
        sqLiteDatabase.execSQL(CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS courses");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS course_date");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS course_times");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS course_locations");
        onCreate(sqLiteDatabase);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Course> getCourses(){
        String query = "SELECT c.id AS course_id, c.department_name, c.group_name, " +
                "t.weekday, t.startTime, t.endTime " +
                "FROM courses AS c " +
                "JOIN course_times AS t ON c.id = t.courseId " +
                "ORDER BY c.id ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        List<Course> courses = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("course_id"));
            List<Triple<String, LocalTime, LocalTime>> foundTimes = new ArrayList<>();
            String department = cursor.getString(cursor.getColumnIndexOrThrow("department_name"));
            String group = cursor.getString(cursor.getColumnIndexOrThrow("group_name"));

            do {
                LocalTime start = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
                LocalTime end = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
                foundTimes.add(new Triple<>(cursor.getString(cursor.getColumnIndexOrThrow("weekday")),
                        start,
                        end));
            }while (cursor.moveToNext() && cursor.getInt(cursor.getColumnIndexOrThrow("course_id")) == id);

            Course course = new Course(department, group, foundTimes, id);
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

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);

        while (cursor.moveToNext()){
            String weekdayString = cursor.getString(cursor.getColumnIndexOrThrow("weekday"));
            if(!rtrn.contains(DayOfWeek.valueOf(weekdayString))) {
                rtrn.add(DayOfWeek.valueOf(weekdayString));
            }
        }
        cursor.close();
        db.close();

        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Pair<LocalTime, LocalTime>> getTimesForWeekday(DayOfWeek weekday){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Pair<LocalTime, LocalTime>> rtrn = new ArrayList<>();
        String[] projection = {"startTime, endTime"};
        String selection = "weekday = ?";
        String[] args = {weekday.name()};

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);

        if(cursor.getCount() == 0){
            return null;
        }

        while (cursor.moveToNext()){
            String startString = cursor.getString(cursor.getColumnIndexOrThrow("startTime"));
            String endString = cursor.getString(cursor.getColumnIndexOrThrow("endTime"));
            rtrn.add(new Pair<LocalTime, LocalTime>(LocalTime.parse(startString), LocalTime.parse(endString)));
        }
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String duration;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues basicInfo = new ContentValues();
        basicInfo.put("department_name", course.getDepartment());
        basicInfo.put("group_name", course.getGroup());
        long rtrn = db.insert("courses", null, basicInfo);

        List<Triple<String, LocalTime, LocalTime>> times = course.getCourseTimes();
        for(Triple<String, LocalTime, LocalTime> time : times){
            ContentValues timeInfo = new ContentValues();
            duration = Long.toString(Duration.between(time.getSecond(), time.getThird()).toMinutes());
            timeInfo.put("weekday", time.getFirst());
            timeInfo.put("startTime", time.getSecond().format(formatter));
            timeInfo.put("endTime", time.getThird().format(formatter));
            timeInfo.put("duration", duration);
            timeInfo.put("courseId", rtrn);
            db.insert("course_times", null, timeInfo);
        }
        return rtrn;
    }

    private Pair<String, String> getBasicInformation(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"department_name, group_name"};
        String selection = "id = ?";
        String[] args = {Long.toString(id)};
        Pair<String, String> rtrn = null;

        Cursor cursor = db.query("courses", projection, selection, args, null, null, null);
        if (cursor.moveToFirst()){
            rtrn = new Pair<>(cursor.getString(cursor.getColumnIndexOrThrow("department_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("group_name")));
        }
        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Course getCourse(DayOfWeek weekday, Pair<LocalTime, LocalTime> timePair){
        SQLiteDatabase db = this.getReadableDatabase();
        Course course = null;
        String[] projection = {"courseId"};
        String selection = "weekday = ? AND startTime = ? AND endTime = ?";
        String[] args = {weekday.name(), timePair.getFirst().toString(), timePair.getSecond().toString()};

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);

        if(cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("courseId"));
            Pair<String, String> basics = getBasicInformation(id);
            if(basics == null) return null;
            course = new Course(basics.getFirst(), basics.getSecond(), id);
        }
        cursor.close();
        db.close();

        return course;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Triple<String, LocalTime, LocalTime>> getTimes(Course course){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"startTime", "endTime", "weekday"};
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        List<Triple<String, LocalTime, LocalTime>> rtrn = new ArrayList<>();

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);

        while (cursor.moveToNext()){
            String day = cursor.getString(cursor.getColumnIndexOrThrow("weekday"));
            LocalTime start = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("startTime")));
            LocalTime end = LocalTime.parse(cursor.getString(cursor.getColumnIndexOrThrow("endTime")));
            rtrn.add(new Triple<>(day, start, end));
        }

        cursor.close();
        db.close();

        return rtrn;
    }

    public void insertLocations(Course course, ArrayList<String> locations){
        SQLiteDatabase db = this.getWritableDatabase();
        String id = course.getStringId();

        for(String loc : locations){
            ContentValues contentValues = new ContentValues();
            contentValues.put("courseId", id);
            contentValues.put("location", loc);
            db.insert("course_locations", null, contentValues);
        }

        db.close();
    }

    public List<String> getLocations(Course course){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"location"};
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        List<String> rtrn = new ArrayList<>();

        Cursor cursor = db.query("course_locations", projection, selection, args, null, null, null);

        while (cursor.moveToNext()){
            String loc = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            rtrn.add(loc);
        }

        return rtrn;
    }

    public boolean deleteCourse(Course course){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("courses", "id = ?", new String[]{course.getStringId()});
            db.delete("course_date", "courseId = ?", new String[]{course.getStringId()});
            db.delete("course_times", "courseId = ?", new String[]{course.getStringId()});
            db.delete("course_locations", "courseId = ?", new String[]{course.getStringId()});
        }catch (SQLException e){
            Log.e("sql deletion error: ", e.toString());
            db.close();
            return false;
        }
        db.close();
        return true;
    }
}
