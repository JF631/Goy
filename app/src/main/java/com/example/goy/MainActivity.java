package com.example.goy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

import java.io.Serializable;
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
public class MainActivity extends AppCompatActivity {

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

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Add the fragment to the layout
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.add(R.id.fragment_container_view, homeFragment);

        // Commit the transaction
        fragmentTransaction.commit();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  requiredPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};

        Toolbar main_bar = findViewById(R.id.main_toolbar);
        setSupportActionBar(main_bar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this,
                    requiredPermissions,
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        geofenceHelper = new GeofenceHelper(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()){
                case R.id.item_dateList:
                    selectedFragment = new DepartmentFragment();
                    break;
                case R.id.item_home:
                    selectedFragment = new HomeFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view, selectedFragment).commit();
            return true;
        });
    }

    private void registerGeofence(){
        geofenceHelper.registerStandardFences();
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
}