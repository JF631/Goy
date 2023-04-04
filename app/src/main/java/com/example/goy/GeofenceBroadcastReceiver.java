package com.example.goy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Geofencing error: " + errorCode);
            return;
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.d(TAG,  "GEOINFO: " + geofencingEvent.toString());
        Log.d(TAG, String.valueOf(transitionType));
        //Log.d(TAG, geofencingEvent.getTriggeringGeofences().get(0).toString());
        if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL){
            Log.d(TAG, "geofence entered");
            Intent serviceIntent = new Intent(context, IntentDatabaseService.class);
            context.startService(serviceIntent);
        }

    }
}
