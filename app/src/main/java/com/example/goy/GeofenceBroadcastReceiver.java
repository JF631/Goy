package com.example.goy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.time.LocalTime;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.toString());
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()){
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Geofencing error: " + errorCode);
            return;
        }
        int transitionType = geofencingEvent.getGeofenceTransition();
        Log.d(TAG, String.valueOf(transitionType));
        if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL){
            List<Geofence> triggeredFences = geofencingEvent.getTriggeringGeofences();
            if (triggeredFences.size() > 1) return;
            Log.d(TAG, "geofence entered: " + geofencingEvent.getTriggeringGeofences());
            if(!notificationHelper.createNotification("Geofence entered", "finally!")){Log.d(TAG, "no notification permission given");}
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DatabaseWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("location", triggeredFences.get(0).getRequestId())
                            .build())
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("GoyPrefs", Context.MODE_PRIVATE);
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
            Log.d(TAG, "entered");
            Pair<String, LocalTime> enteredFence = new Pair<>(geofencingEvent.getTriggeringGeofences().get(0).getRequestId(),
                    LocalTime.now());
            SharedPreferences.Editor sEditor = sharedPreferences.edit();
            sEditor.putString("enteredFence", enteredFence.toString());
            sEditor.apply();

        }
        if(transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
            String enteringTime = sharedPreferences.getString("enteredFence", "");
            if(enteringTime.isEmpty()) return;
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DatabaseWorker.class)
                    .setInputData(new Data.Builder()
                            .putString("location", geofencingEvent.getTriggeringGeofences().get(0).getRequestId())
                            .putString("left", enteringTime)
                            .build())
                    .build();
            WorkManager.getInstance(context).enqueue(workRequest);
        }

    }
}
