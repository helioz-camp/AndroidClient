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
import android.media.AudioManager;
import android.media.SoundPool;




/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {

    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);
    AudioManager mAudioManager;
    int mySound;
    HttpURLConnection connection;

    SoundPool mSoundPool;

    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //setting up the sound controller
        mAudioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
        //SoundPool.Builder builder = new SoundPool.Builder();
        //builder.setAudioAttributes(USAGE_GAME);
        //builder.setMaxStreams(100);
        mSoundPool = new SoundPool(100, AudioManager.STREAM_MUSIC, 0);

        mySound = mSoundPool.load(getContext(), R.raw.sound, 1);

        //= new SoundPool(100, AudioManager.STREAM_MUSIC, 0);

        //set up connection to server
        /*try {
            URL url = new URL("http://10.0.0.4:13231/Users/jqjunk/Desktop/HeliozSoundnasium/repo/audiomixserver/audiomixserver/sounds/0.wav");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
        } catch (IOException error) {
            Log.d("error", "Failed to establish connection to server");
        }*/

        //continue pinging a non-existent url to keep wifi antenna from sleeping
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.0.4:13231/nothing");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(1000);
                    connection.getInputStream();
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
            int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mSoundPool.play(mySound,0.99f,0.99f,1,0,1f);

            try {
                Log.d("server message", callServer());
            } catch (IOException error) {
                Log.d("error", "Failed to call server");
            }
            long stopTime = System.currentTimeMillis();
            Integer elapsedTime = (Integer) ((int)(stopTime - startTime));
            Log.d("myTag", elapsedTime.toString());
        }

        return true;
    }

    private String callServer() throws IOException {
        URL url = new URL("http://10.0.0.4:13231/Users/jqjunk/Desktop/HeliozSoundnasium/repo/audiomixserver/audiomixserver/sounds/0.wav");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(1000);

        InputStream in = new BufferedInputStream(connection.getInputStream());

        String responseBody = "Got no text from server";
        try {
            responseBody = readStream(in);
        } catch (java.net.SocketTimeoutException error) {
            error.printStackTrace(System.out);
        } catch (IOException error) {
            error.printStackTrace(System.out);
        } finally {
            connection.disconnect();
        }
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
