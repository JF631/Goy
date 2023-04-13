package com.example.goy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences sharedPreferences = context.getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
            boolean isGeofenceActive = sharedPreferences.getBoolean("geofenceActive", false);
            if(isGeofenceActive) registerFences(context);
        }
    }

    public void registerFences(Context context){
        GeofenceHelper geofenceHelper = new GeofenceHelper(context);
        geofenceHelper.registerStandardFences();
        Toast.makeText(context, "Fences re-registered", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "fences re-registered");
    }
}
