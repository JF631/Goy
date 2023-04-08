package com.example.goy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            registerFences(context);
        }
    }

    public void registerFences(Context context){
        GeofenceHelper geofenceHelper = new GeofenceHelper(context);
        geofenceHelper.registerStandardFences();
        Log.d(TAG, "fences re-registered");
    }
}
