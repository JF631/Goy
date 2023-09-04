package com.example.goy;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity {

    private GeofenceHelper geofenceHelper;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1, PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2,
            PERMISSIONS_REQUEST_NOTIFICATIONS = 3;
    private static String[] requiredPermissions = {Manifest.permission.ACCESS_FINE_LOCATION} ;
    private SharedPreferences.Editor sharedEditor;
    private static final String[] backgroundLocationPermission = {Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    private static boolean isGeofenceActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        getSharedPrefs();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Remove any existing fragments from the container
        for (Fragment fragment : fragmentManager.getFragments()) {
            fragmentTransaction.remove(fragment);
        }

        // Add the new fragment to the layout
        HomeFragment homeFragment = new HomeFragment();
        fragmentTransaction.replace(R.id.fragment_container_view, homeFragment);

        // Commit the transaction
        fragmentTransaction.commit();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  requiredPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
            Fragment selectedFragment;
            switch (item.getItemId()){
                case R.id.item_dateList:
                    selectedFragment = new DepartmentFragment();
                    break;
                case R.id.item_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.item_files:
                    selectedFragment = new FilesFragment();
                    break;
                default:
                    selectedFragment = new StatsFragment();
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.fragment_container_view, selectedFragment).commit();
            return true;
        });
    }

    public void hideNavBar(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        int height = bottomNavigationView.getHeight();
        bottomNavigationView.animate().translationY(height).setDuration(300);
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showNavBar(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.animate().translationY(0).setDuration(300);
    }

    private void registerGeofence(){
        geofenceHelper.registerStandardFences();
        sharedEditor.putBoolean("geofenceActive", true);
        sharedEditor.apply();
    }

    private void removeGeofence(){
        geofenceHelper.removeStandardFences();
        sharedEditor.putBoolean("geofenceActive", false);
        sharedEditor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    registerGeofence();
                    getSharedPrefs();
                    invalidateOptionsMenu();
                }
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
                getSharedPrefs();
                invalidateOptionsMenu();
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

    private void getSharedPrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();
        isGeofenceActive = sharedPreferences.getBoolean("geofenceActive", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem geoFenceItem = menu.findItem(R.id.activate_geofence);
        geoFenceItem.setChecked(isGeofenceActive);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.activate_geofence:
                boolean isChecked = !item.isChecked();
                if(isChecked){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this,
                                requiredPermissions,
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }else {
                        registerGeofence();
                    }
                }else {
                    removeGeofence();
                }
                item.setChecked(isChecked);
                return true;
            case R.id.edit_personal_details:
                CreatePersonFragment createPersonFragment = new CreatePersonFragment();
                createPersonFragment.show(getSupportFragmentManager(), "create_person");
                return true;
            case R.id.show_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAbout(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Über TRACKS")
                .setMessage("TuS Real-time Activity Check-in and Keeping System\n" +
                        "Kontakt: software_jaf@mx442.de")
                .setCancelable(false)
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView emailTextView = alertDialog.findViewById(android.R.id.message);
        if (emailTextView != null) {
            Pattern pattern = Patterns.EMAIL_ADDRESS;
            Linkify.addLinks(emailTextView, pattern, "mailto:");
        }
    }

}