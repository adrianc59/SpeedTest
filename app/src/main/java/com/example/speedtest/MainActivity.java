package com.example.speedtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speedtest.Foreground.MyIntentService;

public class MainActivity extends Activity {
    public static TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void speedCheck(View v) {
        Toast.makeText(this, "Test enqueued", Toast.LENGTH_SHORT).show();

        Intent intent1 = new Intent(getApplicationContext(), MyIntentService.class);
        startService(intent1);

        //Intent serviceIntent = new Intent(this, ExampleJobIntentService.class);
        //ExampleJobIntentService.enqueueWork(this, serviceIntent);
    }
}