package com.helioz.androiddrumclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends GLSurfaceView {
    // Android likes to TAG each class for Logging
    private static final String TAG = DrumView.class.getSimpleName();
    private static final String LONG_PLAY_INDICATOR = "0";
    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);
    Audiomixclient.PlaybackRequestState repeatingPlayback = null;
    public float touchX = 0;
    public float touchY = 0;
    public long touchStartMillis = 1000000;


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

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES20.glClearColor(0.7f, 0.1f, 0.1f, 1.0f);
                DrumGL.setup();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                DrumGL.draw(DrumView.this);
            }
        });
        setZOrderOnTop(true);
    }

    public void setCurrentSound (String sound) {
        currentSound = sound;
    }

    void stopPlayback() {
        synchronized (this) {
            if (repeatingPlayback != null) {
                repeatingPlayback.stopRepeating();
                repeatingPlayback = null;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartMillis = System.currentTimeMillis();
                touchX = e.getX();
                touchY = e.getY();

                Log.d(TAG, "pointer down x=" + touchX + " y=" + touchY);

                if (currentSound.contains(LONG_PLAY_INDICATOR)) {
                    stopPlayback();
                    repeatingPlayback = Audiomixclient.getInstance(getContext()).repeatSound(currentSound);
                }
                else {
                    StopWatch watch = new StopWatch();
                    try {
                        mp.start();
                    } catch (Exception ex) {
                        Log.w(TAG, "playing media player", ex);
                    }

                    try {
                        Audiomixclient.getInstance(getContext()).playSound(currentSound);
                    } catch (Exception error) {
                        Log.e(TAG, "Failed to call server", error);
                    }
                    Log.d(TAG, "Initiated playing in " + watch);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (currentSound.contains(LONG_PLAY_INDICATOR)) {
                    Audiomixclient.getInstance(getContext()).stopSound(repeatingPlayback);
                    stopPlayback();
                }
                break;
        }
        return true;

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPlayback();
    }


}
