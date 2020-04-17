package com.example.speedtest;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.speedtest.Foreground.MyIntentService;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.spark.submitbutton.SubmitButton;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends Activity {
    public static TextView tv;
    public SubmitButton submitButton;
    public PointerSpeedometer pointerSpeedometer;
    public TextView ipView;
    final Handler handler = new Handler();
    public TextView location;
    private static final int REQUEST_CODE = 101;
    private FusedLocationProviderClient client;
    public String ip = "";
    public String country = "IRELAND";
    public View text3;
    public View text4;

    private Switch wifiSwitch;
    private WifiManager wifiManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        submitButton = findViewById(R.id.button);
        pointerSpeedometer = findViewById(R.id.speedView);
        ipView = findViewById(R.id.ipView);
        location = findViewById(R.id.countryView);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        ip = getPublicIPAddress();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                ipView.setText(ip);
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //country = getCountry();
                location.setText(country);
            }
        }, 2000);


        wifiSwitch = findViewById(R.id.wifi_switch);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    wifiSwitch.setText("WiFi is ON");
                } else {
                    wifiManager.setWifiEnabled(false);
                    wifiSwitch.setText("WiFi is OFF");
                }
            }
        });




        client = LocationServices.getFusedLocationProviderClient(this);



    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiStateReceiver);
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiSwitch.setChecked(true);
                    wifiSwitch.setText("WiFi is ON");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiSwitch.setChecked(false);
                    wifiSwitch.setText("WiFi is OFF");
                    break;
            }
        }
    };


private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
}



    public static String getPublicIPAddress(){
        String value = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> result = es.submit(new Callable<String>() {
            public String call() throws Exception {
                try {
                    URL url = new URL("http://whatismyip.akamai.com/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        urlConnection.disconnect();
                        return total.toString();
                    }finally {
                        urlConnection.disconnect();
                    }
                }catch (IOException e){
                    Log.e("Public IP: ",e.getMessage());
                }
                return null;
            }
        });
        try {
            value = result.get();
        } catch (Exception e) {
            // failed
        }
        es.shutdown();

        return value;
    }


    public static String getCountry(){
        String value = null;
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<String> result = es.submit(new Callable<String>() {
            public String call() throws Exception {
                try {
                    URL url = new URL("https://ipapi.co/78.19.210.80/country_name/");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(in));
                        StringBuilder total = new StringBuilder();
                        String line;
                        while ((line = r.readLine()) != null) {
                            total.append(line).append('\n');
                        }
                        urlConnection.disconnect();
                        return total.toString();
                    }finally {
                        urlConnection.disconnect();
                    }
                }catch (IOException e){
                    Log.e("Country Location: ",e.getMessage());
                }
                return null;
            }
        });
        try {
            value = result.get();
        } catch (Exception e) {
            // failed
        }
        es.shutdown();

        return value;
    }

    public void speedCheck(View v) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pointerSpeedometer.setVisibility(View.VISIBLE);
                PointerSpeedometer pointerSpeedometer= (PointerSpeedometer) findViewById(R.id.speedView);
                submitButton.setVisibility(View.INVISIBLE);
            }
        }, 3700);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pointerSpeedometer.speedTo(50);
            }
        }, 4600);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pointerSpeedometer.setWithPointer(false);
                text3.setVisibility(View.VISIBLE);
                text4.setVisibility(View.VISIBLE);
            }
        }, 13000);




        // change speed to 50 Km/h


        Toast.makeText(this, "Test enqueued", Toast.LENGTH_SHORT).show();

        Intent intent1 = new Intent(getApplicationContext(), MyIntentService.class);
        startService(intent1);

        //Intent serviceIntent = new Intent(this, ExampleJobIntentService.class);
        //ExampleJobIntentService.enqueueWork(this, serviceIntent);
    }


}