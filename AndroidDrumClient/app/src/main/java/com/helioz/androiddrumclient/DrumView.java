package com.helioz.androiddrumclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;



/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {
    // Android likes to TAG each class for Logging
    private static final String TAG = DrumView.class.getSimpleName();
    private static final String LONG_PLAY_INDICATOR = "Long_Play";
    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);

    private String currentSound;

    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        currentSound = "0";
        try {
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Media ready");
                }
            });
            mp.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "preparing media player", e);
        }
    }

    public void setCurrentSound (String sound) {
        currentSound = sound;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (currentSound.contains(LONG_PLAY_INDICATOR)) {
                    //start playing sound until stop signal
                }
                else {
                    long startTime = System.currentTimeMillis();
                    try {
                        mp.start();
                    } catch (Exception ex) {
                        Log.w(TAG, "playing media player", ex);
                    }

                    try {
                        Audiomixclient.getInstance(getContext()).callServer(new Uri.Builder().path("play").appendQueryParameter("sample", currentSound).build());
                    } catch (Exception error) {
                        Log.e(TAG, "Failed to call server", error);
                    }
                    long stopTime = System.currentTimeMillis();
                    Log.d(TAG, "Playing started in " + (stopTime - startTime) + "ms");
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (currentSound.contains(LONG_PLAY_INDICATOR))
                    //send stop playing signal
                break;
        }
        return true;

    }


}
