package com.example.speedtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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

    private MainAdapter adapter;

    public int[] numberProgress = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private Intent serviceIntent;
    private int jobCount = 0;
    private TextView jobCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_images);

        jobCountView = findViewById(R.id.jobCountView);

        gridView = findViewById(R.id.grid_view);
        requestPermission();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));

        serviceIntent = new Intent(this, JobIntentService.class);

        adapter = new MainAdapter(this, numberWord, numberImage, numberProgress);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"Downloading Image " + numberWord[+position],Toast.LENGTH_LONG).show();

                jobCount++;
                jobCountView.setText("Job Count: " + jobCount);

                serviceIntent.putExtra("position", position+1);
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
            String message = intent.getStringExtra("message");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            final int position = intent.getIntExtra("position", 0);

            if(position != -1) {
                DownloadImages.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        numberProgress[+(position - 1)] = 100;
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new MainAdapter(DownloadImages.this, numberWord, numberImage, numberProgress);
                        gridView.setAdapter(adapter);
                    }
                });
            }

            jobCount = intent.getIntExtra("jobCount", 0);
            jobCountView.setText("Job Count: " + jobCount);
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateDownloadPercentage(int imageNumber, int percentage){
        numberProgress[imageNumber-1] = percentage;
    }
}
