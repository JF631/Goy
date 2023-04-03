package com.example.goy;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class IntentDatabaseService extends IntentService {
    private List<Pair<LocalTime, LocalTime>> timeList;
    private static final String TAG = "IntentDatabaseService";
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public IntentDatabaseService(){
        super("insertDateService");
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(this);
        LocalDate date = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        timeList = dataBaseHelper.getTimesForWeekday(date.getDayOfWeek());
        if(timeList == null){
            Log.d(TAG, "no times found for given weekday");
            return;
        }
        for(Pair<LocalTime, LocalTime> time : timeList){
            if(currentTime.isAfter(time.getFirst()) && currentTime.isBefore(time.getSecond())){
                Course course = dataBaseHelper.getCourse(date.getDayOfWeek(), time);
                dataBaseHelper.insertDate(course, date.format(dateFormat));
                Toast.makeText(this, time.toString(), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "not the right time " + time.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        NotificationChannel channel = new NotificationChannel(
                "channel_id",
                "Geofence Alert",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

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
