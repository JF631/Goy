package com.example.goy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceHelper {
    private static final float GEOFENCE_RADIUS = 100;
    private static final String TAG = "GeofenceHelper";


    private Context ctx;
    private GeofencingClient geofencingClient;
    private PendingIntent pendingIntent;

    public GeofenceHelper(Context ctx) {
        this.ctx = ctx;
        this.geofencingClient = LocationServices.getGeofencingClient(ctx);
        this.pendingIntent = createGeofencingPendingIntent();
    }

    @SuppressLint("MissingPermission")
    public void addGeofence(List<Location> locations) {
        List<Geofence> geofenceList = new ArrayList<>();
        for(Location location : locations){
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(location.getName())
                    .setCircularRegion(location.getLatitude(), location.getLongitude(), location.getRadius())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT |Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(100000)
                    .build());
        }
        /**
        Geofence geofence = new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(10000)
                .build();**/

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence added: " + geofenceList.toString()))
                .addOnFailureListener(e -> Log.d(TAG, "failed to add geofence", e));
    }

    public void removeGeofence(String geofenceId){
        List<String> ids = new ArrayList<>();
        ids.add(geofenceId);
        geofencingClient.removeGeofences(ids)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence removed"))
                .addOnFailureListener(e -> Log.d(TAG, "failed to remove geofence", e));
    }

    public boolean checkExistance(String fenceId, Intent intent){
        List<Geofence> triggeringFences = GeofencingEvent.fromIntent(intent).getTriggeringGeofences();
        for(Geofence fence : triggeringFences){
            if(fence.getRequestId().equals(fenceId)) return true;
        }
        return false;
    }


    private PendingIntent createGeofencingPendingIntent(){
        Intent intent = new Intent(ctx, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT );
    }
}
