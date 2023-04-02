package com.example.goy;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class IntentDatabaseService extends IntentService {

    public IntentDatabaseService(){
        super("insertDateService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);

        String msg = "entered geofence";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Geofence Alert")
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, notificationBuilder.build());


    }
}
