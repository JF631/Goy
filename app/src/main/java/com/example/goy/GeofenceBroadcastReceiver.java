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
    private static final String MSGTITLE = "Geofence Crossed";
    private static final String MSG = "You have crossed a geofence";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.toString());
        NotificationHelper notificationHelper = new NotificationHelper(context);
        //if(!notificationHelper.createNotification(MSGTITLE, MSG)){Log.d(TAG, "no notification permission given");}
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()){
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Geofencing error: " + errorCode);
            return;
        }
        if(geofencingEvent.getTriggeringLocation() != null){
            Log.d(TAG,  "GEOINFO: " + geofencingEvent.getTriggeringLocation().toString());
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.d(TAG, String.valueOf(transitionType));
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.d(TAG, "geofence entered");
            if(!notificationHelper.createNotification("Geofence entered", "finally!")){Log.d(TAG, "no notification permission given");}
            Intent serviceIntent = new Intent(context, IntentDatabaseService.class);
            context.startService(serviceIntent);
        }

    }
}
