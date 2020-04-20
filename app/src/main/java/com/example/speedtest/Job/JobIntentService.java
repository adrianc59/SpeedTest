package com.example.speedtest.Job;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JobIntentService extends androidx.core.app.JobIntentService {
    private static final String TAG = "JobIntentService";
    private static int jobCount;
    private boolean success = false;

    public static void enqueueWork(Context context, Intent work) {
        Log.d(TAG, "Enqueue Work");

        enqueueWork(context, JobIntentService.class, 123, work);

        jobCount++;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork");

        int position = intent.getIntExtra("position", 0);

        String imageName = "image" + position;

        File directory = new File(Environment.getExternalStorageDirectory() + "/Downloaded Images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        FTPClient ftp = null;

        String server = "ftp-adr.cirx08.com";
        int portNumber = 21;
        String user = "ftpuser";
        String password = "M3CZ$^B&Mx42WI$KN%Bjfld4Rr";
        String localFile = Environment.getExternalStorageDirectory() + "/Downloaded Images/" + imageName + ".jpg";
        String filename = "Images/" + imageName + ".jpg";

        try {
            ftp = new FTPClient();
            ftp.connect(server, portNumber);
            Log.d("FTP", "Connected. Reply: " + ftp.getReplyString());

            //ftp.enterLocalPassiveMode();

            ftp.login(user, password);
            Log.d("FTP", "Logged in");
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            Log.d("FTP", "Begin Downloading");

            OutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(localFile));

                boolean status = ftp.retrieveFile(filename, outputStream);
                System.out.println("status = " + status);
                System.out.println("reply  = " + ftp.getReplyString());

                if(ftp.getReplyString().trim().equals("226 Transfer complete.")){
                    success = true;
                }
            }catch (Exception e){
                Log.d("Download Image Error", e.getMessage());
            }
            finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"FTP SERVER ERROR",Toast.LENGTH_LONG).show();
        } finally {
            if (ftp != null) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                } catch (IOException e) {
                    System.out.println("FTP Logout Error: " + e.getMessage());
                }
            }
        }

        jobCount--;
        sendMessage(position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onStopCurrentWork() {
        Log.d(TAG, "onStopCurrentWork");
        return super.onStopCurrentWork();
    }

    private void sendMessage(int passedPosition) {
        Log.d("sender", "Broadcasting message");

        int position = 0;
        String message;

        if(success) {
            message = "Download Successful";
            position = passedPosition;
        }
        else {
            message = "Download Failed";
            position = -1;
        }

        Intent intent = new Intent("custom-event-name");
        intent.putExtra("jobCount", jobCount);
        intent.putExtra("message", message);
        intent.putExtra("position", position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}