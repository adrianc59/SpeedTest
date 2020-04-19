package com.example.speedtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speedtest.Job.JobIntentService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DownloadImages extends AppCompatActivity {
    GridView gridView;
    String[] numberWord = {"1", "2","3","4", "5","6","7", "8","9","10","11","12","13","14"};

    int[] numberImage = {R.drawable.image1,R.drawable.image2,R.drawable.image3,R.drawable.image4,
            R.drawable.image5,R.drawable.image6,R.drawable.image7,R.drawable.image8,
            R.drawable.image9,R.drawable.image10,R.drawable.image11,R.drawable.image12,
            R.drawable.image13,R.drawable.image14};


    //private ImageButton image1;
    //private ImageButton image2;
    //private ImageButton image3;
    //private ImageButton image4;

    private Intent serviceIntent;
    private int jobCount = 0;
    private TextView jobCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_images);

        jobCountView = findViewById(R.id.jobCountView);
       //image1 = findViewById(R.id.image1);
        //image2 = findViewById(R.id.image2);
        //image3 = findViewById(R.id.image3);
        //image4 = findViewById(R.id.image4);
        gridView = findViewById(R.id.grid_view);
        requestPermission();

        //image1.setOnClickListener( new DownloadClick());
        //image2.setOnClickListener( new DownloadClick());
        //image3.setOnClickListener( new DownloadClick());
        //image4.setOnClickListener( new DownloadClick());

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));

        serviceIntent = new Intent(this, JobIntentService.class);

        MainAdapter adapter = new MainAdapter(this,numberWord,numberImage);
        gridView.setAdapter(adapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"You Clicked " + numberWord[+position],Toast.LENGTH_SHORT).show();

                jobCount++;
                jobCountView.setText("Job Count: " + jobCount);

                serviceIntent.putExtra("imageName", "image" + numberWord[+position]);
                JobIntentService.enqueueWork(getApplicationContext(), serviceIntent);
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, 1);
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
