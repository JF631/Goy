package com.example.goy;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeofenceHelper {
    private static final String TAG = "GeofenceHelper";
    private final Context ctx;
    private final GeofencingClient geofencingClient;
    private final PendingIntent pendingIntent;

    public GeofenceHelper(Context ctx) {
        this.ctx = ctx;
        this.geofencingClient = LocationServices.getGeofencingClient(ctx);
        this.pendingIntent = createGeofencingPendingIntent();
    }

    @SuppressLint("MissingPermission")
    public void addGeofences(List<Area> locations) {
        List<Geofence> geofenceList = new ArrayList<>();
        for(Area location : locations){
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(location.getName())
                    .setCircularRegion(location.getLatitude(), location.getLongitude(), location.getRadius())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT |Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(30000)
                    .build());
        }

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence added: " + geofenceList))
                .addOnFailureListener(e -> Log.d(TAG, "failed to add geofence", e));
    }

    public void removeGeofences(List<String> geofenceIds){
        geofencingClient.removeGeofences(geofenceIds)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "geofence removed"))
                .addOnFailureListener(e -> Log.d(TAG, "failed to remove geofence", e));
    }

    public boolean checkExistance(String fenceId, Intent intent){
        List<Geofence> triggeringFences = Objects.requireNonNull(GeofencingEvent.fromIntent(intent)).getTriggeringGeofences();
        assert triggeringFences != null;
        for(Geofence fence : triggeringFences){
            if(fence.getRequestId().equals(fenceId)) return true;
        }
        return false;
    }

    public void registerStandardFences(){
        List<Area> locations = new ArrayList<>();
        locations.add(new Area(51.259864, 7.477231, 100, "Sportplatz"));
        locations.add(new Area(51.260517, 7.469787, 100, "Sporthalle"));

        addGeofences(locations);
    }

    public void removeStandardFences(){
        List<String> ids = new ArrayList<>();
        ids.add("Sportplatz");
        ids.add("Sporthalle");
        removeGeofences(ids);
    }

    private PendingIntent createGeofencingPendingIntent(){
        Intent intent = new Intent(ctx, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT );
    }
}
