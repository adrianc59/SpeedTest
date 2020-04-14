package com.example.speedtest.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.example.speedtest.MainActivity;
import com.example.speedtest.R;

public class Utils {
    public static Notification createNotification(Context context, String message){
        String chanelId = "2";
        String chanelName = "Channel Name";

        NotificationChannel channel = new NotificationChannel(chanelId, chanelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setLightColor(Color.BLUE);
        channel.enableLights(true);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(context, "2")
                        .setContentTitle("Title")
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setTicker("Ticker")
                        .build();

        return  notification;
    }
}
