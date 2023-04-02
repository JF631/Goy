package com.example.goy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.e(TAG, "Geofencing error");
            return;
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.d(TAG, "geofence entered");
            Intent serviceIntent = new Intent(context, IntentDatabaseService.class);
            context.startService(serviceIntent);
        }

    }
}
