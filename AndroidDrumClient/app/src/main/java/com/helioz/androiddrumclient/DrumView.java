package com.helioz.androiddrumclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {
    // Android likes to TAG each class for Logging
    private static final String TAG = DrumView.class.getSimpleName();
    private static final int LISTEN_UDP_PORT = 13232;
    private static final int SEND_UDP_PORT = 13231;
    private static final String SERVER_IP = "192.168.0.100";
    private static final int SOCKET_TIMEOUT_MILLIS = 10;
    private static final int RETRIES = 10;

    final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);
    DatagramSocket datagramSocket;
    final AtomicLong requestCount = new AtomicLong(System.currentTimeMillis());

    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);

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

        try {
            datagramSocket = new DatagramSocket(LISTEN_UDP_PORT);
            datagramSocket.setBroadcast(true);
            callServer("reset");
        } catch (IOException e) {
            Log.e(TAG, "DatagramSocket failed " + LISTEN_UDP_PORT, e);
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            long startTime = System.currentTimeMillis();
            try {
                mp.start();
            } catch (Exception ex) {
                Log.w(TAG, "playing media player", ex);
            }

            try {
                callServer();
            } catch (Exception error) {
                Log.e(TAG, "Failed to call server", error);
            }
            long stopTime = System.currentTimeMillis();
            Log.d(TAG, "Playing started in " + (stopTime - startTime) + "ms");
        }

        return true;
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp.ipAddress == 0) {
            throw new IOException("No IP address from WiFi " + dhcp);
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        return InetAddress.getByAddress(new byte[] {
                (byte)(broadcast >>> 24),
                (byte)(broadcast >>> 16),
                (byte)(broadcast >>> 8),
                (byte)broadcast});
    }
    private void callServer() {
        callServer("/Users/jqjunk/Desktop/HeliozSoundnasium/repo/audiomixserver/audiomixserver/sounds/FingerSnap01.wav");
    }

    private void callServer(final String command) {
        InetAddress address = null;
        try {
            try {
                address = InetAddress.getByName(SERVER_IP);
            } finally {
                address = getBroadcastAddress();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not discover broadcast IP", e);
        }
        if (address == null) {
            Log.e(TAG, "Impossible! Can't lookup server");
            return;
        }
        final InetAddress finalAddress = address;
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... objects) {
                try {
                    asyncCallServer(finalAddress, command);
                } catch (Exception e) {
                    Log.e(TAG, "could not call server with " + command, e);
                }
                return null;
            }
        }.execute();
    }

    private void asyncCallServer(InetAddress address, String command) throws IOException  {
        StringBuilder builder = new StringBuilder();
        builder.append("audiomixclient/0 android 0\n")
                .append(requestCount.incrementAndGet())
                .append("\n")
                .append(command + "\n");
        byte[] bytes = builder.toString().getBytes();
        long start = System.currentTimeMillis();
        for (int attempt = 0; RETRIES > attempt; ++attempt) {
            Log.d(TAG, "Sending UDP packet " + builder + " to " + address + " from " + datagramSocket.toString() + " attempt " + attempt);
            datagramSocket.send(new DatagramPacket(bytes, bytes.length, address, SEND_UDP_PORT));
            byte[] recvBuf = new byte[1 << 16];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            datagramSocket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
            try {
                datagramSocket.receive(receivePacket);
            } catch (Exception e) {
                Log.w(TAG, "failed to get response attempt " + attempt + " after " + (System.currentTimeMillis() - start) + "ms");
                continue;
            }
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            Log.d(TAG, "Received " + receivePacket.getLength() + " bytes after " + (System.currentTimeMillis() - start) + "ms: " +
                 response);

            return;
        }

        Log.e(TAG, "No response from server " + address + " after " +  (System.currentTimeMillis() - start) + "ms");
    }

}
