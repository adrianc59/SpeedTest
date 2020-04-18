package com.example.speedtest.RBS;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

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

public class MessengerService extends Service {
    public static final int MSG_SAY_HELLO = 1;
    public static final int MSG_GET_IP = 2;
    public static final int MSG_GET_IP_RESPONSE = 2;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_IP:
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
                                Log.d("Public IP: ",e.getMessage());
                            }
                            return "Private";
                        }
                    });
                    try {
                        value = result.get();
                    } catch (Exception e) {
                        Log.d("Messenger Service Error", e.getMessage());
                    }
                    es.shutdown();

                    Message resp = Message.obtain(null, MSG_GET_IP_RESPONSE);
                    Bundle bResp = new Bundle();
                    bResp.putString("respData", value);
                    resp.setData(bResp);

                    try {
                        msg.replyTo.send(resp);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }
}
