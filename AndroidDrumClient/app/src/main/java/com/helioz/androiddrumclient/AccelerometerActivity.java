package com.helioz.androiddrumclient;

import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.media.MediaPlayer;
import android.content.Context;



public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {
    //MediaPlayer mp;
    private SensorManager sensorManager;
    private long lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        Log.d("myTag", "created accelerometer activity");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            getAccelerometer(event);
        }

    }


    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Log.d("myTag", "accelerometer called");

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 1) {
            long startTime = System.currentTimeMillis();
            //mp = MediaPlayer.create(this, R.raw.sound);
            //mp.start();
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






    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
