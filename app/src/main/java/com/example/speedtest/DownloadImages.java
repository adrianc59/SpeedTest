package com.example.speedtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speedtest.Job.JobIntentService;

public class DownloadImages extends AppCompatActivity {

    private WifiReceiver wifiReceiver = new WifiReceiver();

    private ImageButton image1;
    private ImageButton image2;
    private ImageButton image3;
    private ImageButton image4;

    private Intent serviceIntent;
    private int jobCount = 0;
    private TextView jobCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_images);

        jobCountView = findViewById(R.id.jobCountView);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);

        image1.setOnClickListener( new DownloadClick());
        image2.setOnClickListener( new DownloadClick());
        image3.setOnClickListener( new DownloadClick());
        image4.setOnClickListener( new DownloadClick());

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));

        serviceIntent = new Intent(this, JobIntentService.class);
    }

    private void imageClick(String imageName) {
        Toast.makeText(this, "YAY", Toast.LENGTH_SHORT).show();

        jobCount++;
        jobCountView.setText("Job Count: " + jobCount);

        serviceIntent.putExtra("imageName", imageName);
        JobIntentService.enqueueWork(getApplicationContext(), serviceIntent);
    }

    class DownloadClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String resourceName = v.getResources().getResourceName(v.getId());
            String[] id = resourceName.split("/");

            imageClick(id[1]);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            jobCount = intent.getIntExtra("jobCount", 0);
            jobCountView.setText("Job Count: " + jobCount);
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
