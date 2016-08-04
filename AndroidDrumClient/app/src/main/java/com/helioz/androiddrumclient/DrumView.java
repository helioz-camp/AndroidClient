package com.helioz.androiddrumclient;

import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.media.MediaPlayer;
import java.util.Timer;
import java.util.TimerTask;





/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {

    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);

    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //continue pinging a non-existent server to keep wifi antenna from sleeping
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.0.5:5000/nothing");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.disconnect();
                }   catch (java.net.MalformedURLException error) {}
                catch (IOException error) {}
            }
        },0,200);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            long startTime = System.currentTimeMillis();
            mp.start();
            try {
                Log.d("myTag", callServer());
            } catch (IOException error) {
                //error.printStackTrace(System.out);
                Log.d("myTag", "Failed to call server");
                //throw new RuntimeException(error);
            }
            long stopTime = System.currentTimeMillis();
            Integer elapsedTime = (Integer) ((int)(stopTime - startTime));
            Log.d("myTag", elapsedTime.toString());
        }

        return true;
    }

    private String callServer() throws IOException {
        URL url = new URL("http://10.0.0.5:5000/direct");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String responseBody = "Got no text from server";
        try {
            responseBody = readStream(in);
        } catch (java.net.SocketTimeoutException error) {
            error.printStackTrace(System.out);
            //throw new java.net.SocketTimeoutException();
        } catch (IOException error) {
            error.printStackTrace(System.out);
            //throw new RuntimeException(error);
        }
        connection.disconnect();
        return responseBody;
    }

    private String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }


}
