package com.example.speedtest;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

    private final int progressMax = 100;
    private static String file_url = "http://ipv4.download.thinkbroadband.com/5MB.zip";
    private String rateValue;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationCompat.Builder notification = startMyOwnForeground();
        startForeground(1, notification.build());

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
                int progress = (int) ((total * 100) / lenghtOfFile);
                notification.setProgress(progressMax, progress, false);
                notification.setContentText(progress + "%");
                startForeground(1, notification.build());
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

    private NotificationCompat.Builder startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.speedtest";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Calculating Download Speed")
                .setContentText("0%")
                .setOnlyAlertOnce(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setProgress(progressMax, 0, false)
                .setCategory(Notification.CATEGORY_SERVICE);

        return notification;
    }
}
