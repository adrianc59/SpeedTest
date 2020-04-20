package com.example.speedtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import androidx.appcompat.widget.Toolbar;

import com.example.speedtest.Foreground.MyIntentService;
import com.example.speedtest.RBS.MessengerService;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.spark.submitbutton.SubmitButton;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener {
    public static Button submitButton;
    public static PointerSpeedometer pointerSpeedometer;

    public TextView ipView, location, country, ispProvider, county;
    @SuppressLint("StaticFieldLeak")
    public static TextView text3, text4;
    public String userIp;

    public static Switch wifiSwitch;
    private WifiManager wifiManager;

    ServiceConnection mConnection;
    Messenger mService = null;
    boolean mBound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d("Service Connection","Connecting");
                mService = new Messenger(service);
                mBound = true;
                Log.d("Service Connection","Connected");

                getIP();
            }

            public void onServiceDisconnected(ComponentName className) {
                mService = null;
                mBound = false;
                Log.d("Service Connection","Disconnected");
            }
        };

        bindService(new Intent(this, MessengerService.class), mConnection, Context.BIND_AUTO_CREATE);

        requestPermission();

        submitButton = findViewById(R.id.button);
        pointerSpeedometer = findViewById(R.id.speedView);
        ipView = findViewById(R.id.ipView);
        location = findViewById(R.id.countryView);
        ispProvider = findViewById(R.id.ispProvider);
        county = findViewById(R.id.countyView);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        country = findViewById(R.id.countryView);
        wifiSwitch = findViewById(R.id.wifi_switch);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiSwitch.setOnCheckedChangeListener(this);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        android.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);
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
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        unregisterReceiver(wifiStateReceiver);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION}, 1);
    }

    public void apiCall(String userIp){
        Toast.makeText(MainActivity.this, "Fetching Your Info", Toast.LENGTH_SHORT).show();

        Message msg = Message.obtain(null, MessengerService.MSG_GET_INFO, userIp);

        msg.replyTo = new Messenger(new ResponseHandler());

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void speedCheck(View v) {
        Intent intent1 = new Intent(getApplicationContext(), MyIntentService.class);
        startService(intent1);

        wifiSwitch.setClickable(false);
        wifiSwitch.setVisibility(View.INVISIBLE);
    }

    public void getIP() {
        Toast.makeText(MainActivity.this, "Fetching Your IP", Toast.LENGTH_SHORT).show();
        Message msg = Message.obtain(null, MessengerService.MSG_GET_IP);

        msg.replyTo = new Messenger(new ResponseHandler());

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int respCode = msg.what;

            switch (respCode) {
                case MessengerService.MSG_GET_IP_RESPONSE: {
                    String result = msg.getData().getString("respData");
                    if(result.equalsIgnoreCase("Private")) {
                        ipView.setText("IP: " + result);
                        ispProvider.setText("ISP: Private");
                        county.setText("County: Private");
                        country.setText("Country: Private");
                    }
                    else {
                        ipView.setText(result);
                        apiCall(result);
                    }
                }

                case MessengerService.MSG_GET_INFO_RESPONSE: {
                    String countyValue = msg.getData().getString("respCounty");
                    String countryValue = msg.getData().getString("respCountry");
                    String ispProviderValue = msg.getData().getString("respIspProvider");

                    if (ispProviderValue != null){

                        ispProvider.setText(ispProviderValue.split(" ")[0]);
                    }
                    country.setText(countryValue);
                    county.setText(countyValue);

                }
            }
        }
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiSwitch.setChecked(true);
                    wifiSwitch.setText("WiFi is ON");

                    submitButton.setClickable(false);

                    if(mBound == true){
                        getIP();
                    }

                    submitButton.setClickable(true);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiSwitch.setChecked(false);
                    wifiSwitch.setText("WiFi is OFF");

                    submitButton.setClickable(false);

                    if(mBound == true){
                        getIP();
                    }

                    submitButton.setClickable(true);
                    break;
            }
        }
    };

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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pages2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.item1:
                Intent i = new Intent(getBaseContext(), DownloadImages.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}