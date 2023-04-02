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
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

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
    private SQLiteDatabase db;
    private List<Course> courseList;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_bar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(main_bar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        add = (FloatingActionButton) findViewById(R.id.main_add);
        hour_view = (RecyclerView) findViewById(R.id.main_hours);
        dbHelper = new DataBaseHelper(this);

        courseList = dbHelper.getCourses();
        courseAdapter = new CourseAdapter(courseList);
        hour_view.setLayoutManager(new LinearLayoutManager(this));
        hour_view.setAdapter(courseAdapter);
        courseAdapter.setOnItemClickListener(new CourseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                LocalDate currentDate = LocalDate.now();
                String dateString = currentDate.format(dateFormat);
                if(dbHelper.insertDate(courseList.get(position), dateString)){
                    Toast.makeText(MainActivity.this, "date inserted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "Date is already present in table: " + dbHelper.getDates(courseList.get(position)).toString(), Toast.LENGTH_SHORT).show();
                }

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
    }

    private void showFragment(){
        CreateFragment createFragment = new CreateFragment();
        createFragment.show(getSupportFragmentManager(), "create_course");
        createFragment.setOnCreateCourseClickedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //granted
            }
            else {
                //denied
                Toast.makeText(this, "sorry, but this app needs your location to work properly", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreateCourseClicked(List<String> selectedDays, String department, String group, LocalTime start, LocalTime end) {
        TimeRange range = new TimeRange(start, end, selectedDays);
        Course course = new Course(department, group, range);
        long id = dbHelper.insertCourse(course);
        course.setId(id);
        courseAdapter.insertItem(course);
    }
}