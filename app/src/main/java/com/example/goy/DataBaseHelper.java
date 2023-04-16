package com.example.goy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedules.db";
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int DATABASE_VERSION = 2;
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
                    "date DATE, " +
                    "duration TEXT, " +
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

            Course course = new Course(department, group, foundTimes, id, null);
            courses.add(course);
            cursor.moveToPrevious();
        }
        db.close();
        cursor.close();
        setLocations(courses);
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
            rtrn.add(new Pair<>(LocalTime.parse(startString), LocalTime.parse(endString)));
        }
        cursor.close();
        db.close();

        return rtrn;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean dateExists(Course course, LocalDate date){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"date"};
        String selection = "courseId = ? AND date = ?";
        String[] args = {course.getStringId(), date.format(formatter)};

        Cursor cursor = db.query("course_date", projection, selection, args, null, null, null);

        boolean rtrn = cursor.moveToFirst();
        db.close();
        cursor.close();
        return rtrn;
    }

    public String getDuration(Course course, DayOfWeek dayOfWeek){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"duration"};
        String selection = "courseId = ? AND weekday = ?";
        String[] args = {course.getStringId(), dayOfWeek.toString()};

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);
        if(cursor.moveToFirst()){
            return cursor.getString(cursor.getColumnIndexOrThrow("duration"));
        }
        cursor.close();
        db.close();
        return null;
    }

    public List<String> getWeekDays(Course course){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"weekday"};
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        List<String> weekdays = new ArrayList<>();

        Cursor cursor = db.query("course_times", projection, selection, args, null, null, null);
        while (cursor.moveToNext()){
            String day = cursor.getString(cursor.getColumnIndexOrThrow("weekday"));
            if(!weekdays.contains(day)){
                weekdays.add(day);
            }
        }
        cursor.close();
        db.close();
        return weekdays;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean insertDate(Course course, LocalDate date){
        if(course.getId() == -1){
            Log.e("Course Failure: ", "couldn't get course id" );
            return false;
        }
        String duration = getDuration(course, date.getDayOfWeek());
        if(dateExists(course, date)){return false;}
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("courseId", course.getId());
        values.put("date", date.format(formatter));
        values.put("duration", duration);
        db.insert("course_date", null, values);
        db.close();
        return true;
    }

    public boolean deleteDate(Course course, LocalDate localDate){
        String date = localDate.format(formatter);
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "courseId = ? AND date = ?";
        String[] args = {course.getStringId(), date};

        return db.delete("course_date", selection, args) > 0;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<LocalDate> getDates(Course course, boolean desc){
        SQLiteDatabase db = this.getReadableDatabase();
        List<LocalDate> rtrn = new ArrayList<>();
        String[] projection = {"date"};
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        String orderBy = "date " + (desc ? "DESC" : "ASC");

        Cursor cursor = db.query("course_date", projection, selection, args, null, null, orderBy);

        while (cursor.moveToNext()){
            String dateString = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            Log.d("TEST: ", "date: " + LocalDate.parse(dateString, formatter).format(formatter));
            rtrn.add(LocalDate.parse(dateString, formatter));
        }
        cursor.close();
        db.close();

        return rtrn;
    }

    private List<Course> getCourses(String department){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Course> courses = new ArrayList<>();
        String[] projection = {"id", "group_name"};
        String selection = "department_name = ?";
        String[] args = {department};

        Cursor idCursor = db.query("courses", projection, selection, args, null, null, null);
        while (idCursor.moveToNext()){
            int id = idCursor.getInt(idCursor.getColumnIndexOrThrow("id"));
            String group = idCursor.getString(idCursor.getColumnIndexOrThrow("group_name"));
            Course course = new Course(department, group, id);
            courses.add(course);
        }
        idCursor.close();
        return courses;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Pair<Course, LocalDate>> getDates(@NonNull String department, @Nullable LocalDate start, @Nullable LocalDate end){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Pair<Course, LocalDate>> rtrn = new ArrayList<>();
        List<Course> courses = getCourses(department);
        String[] projection = {"date"};
        String selection = "courseId = ?";
        List<String> args = new ArrayList<>();
        if(start != null){
            selection += " AND date >= ?";
            args.add(start.format(formatter));
        }
        if (end != null){
            selection += " AND date <= ?";
            args.add(end.format(formatter));
            Log.d("args: ", args.toString());
        }
        for(Course course : courses){
            args.add(0, course.getStringId());
            Cursor cursor = db.query("course_date", projection, selection, args.toArray(new String[0]), null, null, null);
            while (cursor.moveToNext()){
                String courseDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                LocalDate cDate = LocalDate.parse(courseDate, formatter);
                Pair<Course, LocalDate> courseDatePair = new Pair<>(course, cDate);
                rtrn.add(courseDatePair);
            }
            cursor.close();
            args.remove(0);
        }
        db.close();
        return rtrn;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertCourse(Course course){
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues basicInfo = new ContentValues();
        basicInfo.put("department_name", course.getDepartment());
        basicInfo.put("group_name", course.getGroup());
        long rtrn = db.insert("courses", null, basicInfo);

        List<Triple<String, LocalTime, LocalTime>> times = course.getCourseTimes();
        for(Triple<String, LocalTime, LocalTime> time : times){
            ContentValues timeInfo = new ContentValues();
            long durationMins = Duration.between(time.getSecond(), time.getThird()).toMinutes();
            Log.d("DBHelper", Long.toString(durationMins));
            double durationHours = durationMins / 60.0;
            String duration = decimalFormat.format(durationHours);
            timeInfo.put("weekday", time.getFirst());
            timeInfo.put("startTime", time.getSecond().format(timeFormatter));
            timeInfo.put("endTime", time.getThird().format(timeFormatter));
            timeInfo.put("duration", duration);
            timeInfo.put("courseId", rtrn);
            db.insert("course_times", null, timeInfo);
        }

        Set<String> locations = course.getLocations();
        for (String loc : locations){
            ContentValues locInfo = new ContentValues();
            locInfo.put("location", loc);
            locInfo.put("courseId", rtrn);
            db.insert("course_locations", null, locInfo);
        }

        db.close();
        return rtrn;
    }

    public void updateBasicGroupInfo(Course course, String identifier, String newValue){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(identifier, newValue);
        String where = "id = ?";
        String[] args = {course.getStringId()};
        db.update("courses", contentValues, where, args);
        db.close();
    }

    public void updateCourseTimes(Course course, List<Triple<String, LocalTime, LocalTime>> newTimes){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        db.delete("course_times", selection, args);
        db.close();

        insertCourseTimes(course, newTimes);
    }

    private void insertCourseTimes(Course course, List<Triple<String, LocalTime, LocalTime>> newTimes){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for(Triple<String, LocalTime, LocalTime> time : newTimes){
            ContentValues timeInfo = new ContentValues();
            long durationMins = Duration.between(time.getSecond(), time.getThird()).toMinutes();
            Log.d("DBHelper", Long.toString(durationMins));
            double durationHours = durationMins / 60.0;
            String duration = decimalFormat.format(durationHours);
            timeInfo.put("weekday", time.getFirst());
            timeInfo.put("startTime", time.getSecond().format(timeFormatter));
            timeInfo.put("endTime", time.getThird().format(timeFormatter));
            timeInfo.put("duration", duration);
            timeInfo.put("courseId", args[0]);
            db.insert("course_times", null, timeInfo);
        }
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
        cursor.close();
        db.close();
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
    public List<Triple<String, LocalTime, LocalTime>> getTimes(@NonNull Course course){
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

    public void insertLocations(@NonNull Course course, @NonNull Set<String> locations){
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

    public List<String> getLocations(@NonNull Course course){
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
        cursor.close();
        db.close();
        return rtrn;
    }

    private void setLocations(@NonNull List<Course> courses){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {"location"};
        String selection = "courseId = ?";

        for(Course course : courses){
            String[] args = {course.getStringId()};
            Set<String> locations = new HashSet<>();
            Cursor cursor = db.query("course_locations", projection, selection, args, null, null, null);

            while (cursor.moveToNext()){
                locations.add(cursor.getString(cursor.getColumnIndexOrThrow("location")));
            }
            cursor.close();
            course.setLocations(locations);
        }
        db.close();
    }

    public void updateLocations(Course course, Set<String> locations){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "courseId = ?";
        String[] args = {course.getStringId()};
        db.delete("course_locations", selection, args);
        db.close();

        insertLocations(course, locations);
    }

    public boolean deleteCourse(@NonNull Course course){
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
