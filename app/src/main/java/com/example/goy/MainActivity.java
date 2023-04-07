package com.example.goy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements CreateFragment.OnCreateCourseClickedListener {

    private FloatingActionButton add;
    private RecyclerView hour_view;
    private Toolbar main_bar;
    private DataBaseHelper dbHelper;
    private CourseAdapter courseAdapter;
    private List<Course> courseList;
    private GeofenceHelper geofenceHelper;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1, PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2,
            PERMISSIONS_REQUEST_NOTIFICATIONS = 3;
    private static String[] requiredPermissions = {Manifest.permission.ACCESS_FINE_LOCATION} ;
    private static final String[] backgroundLocationPermission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  requiredPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};

        main_bar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_bar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this,
                    requiredPermissions,
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        add = (FloatingActionButton) findViewById(R.id.main_add);
        hour_view = (RecyclerView) findViewById(R.id.main_hours);
        dbHelper = new DataBaseHelper(this);
        geofenceHelper = new GeofenceHelper(this);

        courseList = dbHelper.getCourses();
        Log.d("MAIN", courseList.toString());
        courseAdapter = new CourseAdapter(courseList);
        hour_view.setLayoutManager(new LinearLayoutManager(this));
        hour_view.setAdapter(courseAdapter);
        courseAdapter.setOnItemClickListener(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                List<LocalDate> dates = dbHelper.getDates(courseList.get(position));
                List<String> locs = dbHelper.getLocations(courseList.get(position));
                String times = dbHelper.getTimes(courseList.get(position)).toString();
                Toast.makeText(MainActivity.this, "Dates: " + times, Toast.LENGTH_SHORT).show();
            }
        });

        courseAdapter.setOnItemLongClickListener(new CourseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int pos) {
                if(pos != RecyclerView.NO_POSITION) {
                    if(dbHelper.deleteCourse(courseList.get(pos))) {
                        courseAdapter.deleteItem(pos);
                        //Toast.makeText(MainActivity.this, courseList.get(pos).getDepartment(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFragment();
            }
        });

        add.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                LocalDate date = LocalDate.now();
                LocalTime start = LocalTime.parse("15:43");
                LocalTime end = LocalTime.parse("16:43");
                Course course = dbHelper.getCourse(date.getDayOfWeek(), new Pair<>(start, end));
                List<Pair<LocalTime, LocalTime>> time = dbHelper.getTimesForWeekday(date.getDayOfWeek());
                if(course == null) return false;
                Toast.makeText(MainActivity.this, course.toString(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void registerGeofence(){
        List<Area> locations = new ArrayList<>();
        locations.add(new Area(51.259864, 7.477231, 100, "Sportplatz"));
        locations.add(new Area(51.260517, 7.469787, 200, "Sporthalle"));
        geofenceHelper.addGeofence(locations);
    }

    private void showFragment(){
        CreateFragment createFragment = new CreateFragment();
        createFragment.show(getSupportFragmentManager(), "create_course");
        createFragment.setOnCreateCourseClickedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) registerGeofence();
                //granted
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, backgroundLocationPermission, PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
                }
            } else {
                //denied
                Toast.makeText(this, "sorry, but this app needs your location to work properly", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "thanks for allowing location access", Toast.LENGTH_SHORT).show();
                registerGeofence();
            }else {
                Toast.makeText(this, "sorry, but this app needs your location to work properly", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == PERMISSIONS_REQUEST_NOTIFICATIONS){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "thanks for allowing notifications", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "sorry, but this app needs your location to work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateCourseClicked(List<Triple<String, LocalTime, LocalTime>> selectedTimes, String department, String group, ArrayList<String> locations) {
        Course course = new Course(department, group, selectedTimes, locations);
        long id = dbHelper.insertCourse(course);
        if(id == -1) Log.e("FATAL", "couldn't add course");
        Log.d("id: ", Long.toString(id));
        course.setId(id);
        courseAdapter.insertItem(course);
    }
}