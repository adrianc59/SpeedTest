package com.example.speedtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.speedtest.Job.JobIntentService;

public class DownloadImages extends AppCompatActivity {

    private Button image1;
    private Button image2;
    private Button image3;
    private Button image4;

    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_images);

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);

        image1.setOnClickListener( new DownloadClick());
        image2.setOnClickListener( new DownloadClick());
        image3.setOnClickListener( new DownloadClick());
        image4.setOnClickListener( new DownloadClick());

        serviceIntent = new Intent(this, JobIntentService.class);
    }

    private void imageClick(String imageName) {
        Toast.makeText(this, "YAY", Toast.LENGTH_SHORT).show();

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
}
