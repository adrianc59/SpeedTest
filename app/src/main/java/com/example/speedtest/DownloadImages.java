package com.example.speedtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    int[] numberProgress = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};




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

        MainAdapter adapter = new MainAdapter(this,numberWord,numberImage, numberProgress);
        gridView.setAdapter(adapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(),"You Clicked " + numberWord[+position],Toast.LENGTH_SHORT).show();

                jobCount++;
                jobCountView.setText("Job Count: " + jobCount);

                serviceIntent.putExtra("imageName", "image" + numberWord[+position]);
                JobIntentService.enqueueWork(getApplicationContext(), serviceIntent);
                numberProgress[+position] = 100;
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
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateDownloadPercentage(int imageNumber, int percentage){
        numberProgress[imageNumber-1] = percentage;
    }
}
