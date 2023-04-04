package com.example.goy;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private Context context;
    private NotificationManager mNotificationManager;
    public NotificationHelper(Context context) {
        this.context = context;
    }

    public boolean createNotification(String title, String message) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)){
            return false;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, context.getResources().getString(R.string.location_notification_channel_id))
                .setSmallIcon(com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_disabled)
                .setContentTitle(title)
                .setContentText(message);

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(context.getResources().getString(R.string.location_notification_channel_id),
                    context.getResources().getString(R.string.location_notification_channel_name), importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            mBuilder.setChannelId(context.getResources().getString(R.string.location_notification_channel_id));
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(0, mBuilder.build());
        return true;
    }
}
