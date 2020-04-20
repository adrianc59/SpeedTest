package com.example.speedtest.RBS;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.example.speedtest.JsonPlaceHolderApi;
import com.example.speedtest.MainActivity;
import com.example.speedtest.Post;

import org.json.JSONObject;

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

public class MessengerService extends Service {
    public static final int MSG_GET_IP = 1;
    public static final int MSG_GET_IP_RESPONSE = 2;
    public static final int MSG_GET_INFO = 3;
    public static final int MSG_GET_INFO_RESPONSE = 4;

    String country, county, ispProvider;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_IP:
                    SystemClock.sleep(3000);

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
                                        total.append(line).append(' ');
                                    }
                                    urlConnection.disconnect();
                                    return total.toString();
                                }finally {
                                    urlConnection.disconnect();
                                }
                            }catch (IOException e){
                                Log.d("Messenger Get IP: ",e.getMessage());
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

                case MSG_GET_INFO:
                    final String userIp = msg.obj.toString();

                    String value2 = null;
                    ExecutorService es2 = Executors.newSingleThreadExecutor();
                    Future<String> result2 = es2.submit(new Callable<String>() {
                        public String call() throws Exception {
                            try {
                                URL url = new URL("http://ip-api.com/json/" + userIp.split(" ")[0] + "?fields=country,city,isp");
                                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                                try {
                                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                                    BufferedReader r = new BufferedReader(new InputStreamReader(in));
                                    StringBuilder total = new StringBuilder();
                                    String line;
                                    while ((line = r.readLine()) != null) {
                                        total.append(line).append(' ');
                                    }
                                    urlConnection.disconnect();
                                    return total.toString();
                                }finally {
                                    urlConnection.disconnect();
                                }
                            }catch (IOException e){
                                Log.d("Messenger Get IP: ",e.getMessage());
                            }
                            System.out.println("Broken");
                            return "Broke";
                        }
                    });

                    try {
                        value2 = result2.get();
                    } catch (Exception e) {
                        Log.d("Messenger Service Error", e.getMessage());
                    }
                    es2.shutdown();

                    try {
                        JSONObject obj = new JSONObject(String.valueOf(value2));
                        county = obj.getString("city");
                        country = obj.getString("country");
                        ispProvider = obj.getString("isp");
                    }
                    catch (Exception e){
                        System.out.println("Error: " + e);
                    }

                    Message resp2 = Message.obtain(null, MSG_GET_INFO_RESPONSE);
                    Bundle bResp2 = new Bundle();
                    bResp2.putString("respCounty", county);
                    bResp2.putString("respCountry", country);
                    bResp2.putString("respIspProvider", ispProvider);
                    resp2.setData(bResp2);

                    try {
                        msg.replyTo.send(resp2);
                        System.out.println("Info Sent back to main");
                    } catch (RemoteException e) {
                        System.out.println("Error in sending info back: " + e.getMessage());
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
        return mMessenger.getBinder();
    }
}
