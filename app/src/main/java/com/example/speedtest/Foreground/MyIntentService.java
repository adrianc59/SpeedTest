package com.example.speedtest.Foreground;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.speedtest.MainActivity;
import com.example.speedtest.R;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MyIntentService extends IntentService {

    private final int progressMax = 100;
    private static String file_url = "http://ipv4.download.thinkbroadband.com/10MB.zip";
    private String rateValue;
    private float currentRate;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationCompat.Builder notification = startMyOwnForeground();
        startForeground(1, notification.build());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                MainActivity.pointerSpeedometer.speedTo(0);
                MainActivity.pointerSpeedometer.setWithTremble(false);
                MainActivity.pointerSpeedometer.setVisibility(View.VISIBLE);
                MainActivity.submitButton.setVisibility(View.INVISIBLE);
            }
        });

        int count;
        try {
            URL url = new URL(file_url);

            long startTime = System.currentTimeMillis();
            URLConnection conection = url.openConnection();
            conection.connect();

            int lenghtOfFile = conection.getContentLength();

            InputStream input = new BufferedInputStream(url.openStream(),8192);

            byte data[] = new byte[1024];

            long total = 0;
            long currentTime;

            boolean sent = false;

            while ((count = input.read(data)) != -1) {
                total += count;
                int progress = (int) ((total * 100) / lenghtOfFile);

                currentTime = System.currentTimeMillis();

                double temp = (((double)(total / 1024) / ((currentTime - startTime) / 1000)) * 8);
                temp = Math.round( temp * 100.0 ) / 100.0;
                currentRate = (float)temp/1024;
                rateValue = String.valueOf(temp / 1024).concat(" Mbps");
                System.out.println(rateValue);

                if(progress > 4 && !sent) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            MainActivity.pointerSpeedometer.setWithTremble(true);
                            MainActivity.pointerSpeedometer.speedTo(currentRate);
                        }
                    });

                    sent = true;
                }

                notification.setProgress(progressMax, progress, false);
                notification.setContentText(progress + "%");
                startForeground(1, notification.build());
            }

            long endTime = System.currentTimeMillis(); //maybe

            double rate = (((lenghtOfFile / 1024) / ((endTime - startTime) / 1000)) * 8);
            rate = Math.round( rate * 100.0 ) / 100.0;

            if(rate > 1000)
                rateValue = String.format("%.2f", (rate / 1024));
            else
                rateValue = String.format("%.2f", rate).concat(" Kbps");

            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                MainActivity.pointerSpeedometer.speedTo(0);
                MainActivity.pointerSpeedometer.setVisibility(View.INVISIBLE);
                MainActivity.submitButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "Test Complete: " + rateValue, Toast.LENGTH_SHORT).show();

        MainActivity.text3.setText(rateValue);
        MainActivity.text3.setVisibility(View.VISIBLE);
        MainActivity.text4.setVisibility(View.VISIBLE);
        MainActivity.wifiSwitch.setClickable(true);
        MainActivity.wifiSwitch.setVisibility(View.VISIBLE);
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
