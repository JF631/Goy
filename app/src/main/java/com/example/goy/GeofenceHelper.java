package com.example.goy;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofenceHelper {
    private static final float GEOFENCE_RADIUS = 150;
    private static final String TAG = "GeofenceHelper";


    private Context ctx;
    private GeofencingClient geofencingClient;
    private PendingIntent pendingIntent;

    public GeofenceHelper(Context ctx) {
        this.ctx = ctx;
        geofencingClient = LocationServices.getGeofencingClient(ctx);
        pendingIntent = createGeofencingPendingIntent();
    }

    public void addGeofence(double latitude, double longitude, String geofenceId) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(geofenceId)
                .setCircularRegion(latitude, longitude, GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(0)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence)
                .build();

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence added"))
                .addOnFailureListener(e -> Log.d(TAG, "failed to add geofence", e));
    }

    public void removeGeofence(String geofenceId){
        List<String> ids = new ArrayList<>();
        ids.add(geofenceId);
        geofencingClient.removeGeofences(ids)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence removed"))
                .addOnFailureListener(e -> Log.d(TAG, "failed to remove geofence", e));
    }


    private PendingIntent createGeofencingPendingIntent(){
        Intent intent = new Intent(ctx, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
