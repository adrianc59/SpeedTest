package com.example.speedtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends Activity {
    public static TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.text);

        Intent intent = new Intent(this, MyIntentService.class);
        startService(intent);
    }
}