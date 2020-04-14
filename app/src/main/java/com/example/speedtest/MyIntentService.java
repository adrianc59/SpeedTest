package com.example.speedtest;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MyIntentService extends IntentService {

    private static String file_url = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private String rateValue;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Notification notification = startMyOwnForeground();
        startForeground(1, notification);

        int count;
        try {
            URL url = new URL(file_url);

            long startTime = System.currentTimeMillis();
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress("" + (int) ((total * 100) / lenghtOfFile));
            }

            long endTime = System.currentTimeMillis(); //maybe

            double rate = (((lenghtOfFile / 1024) / ((endTime - startTime) / 1000)) * 8);
            rate = Math.round( rate * 100.0 ) / 100.0;

            if(rate > 1000)
                rateValue = String.valueOf(rate / 1024).concat(" Mbps");
            else
                rateValue = String.valueOf(rate).concat(" Kbps");

            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "Test Complete: " + rateValue, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Test Started", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }

    private Notification startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.speedtest";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        return notification;
    }
}
