package com.helioz.androiddrumclient;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by john on 8/22/16.
 */
class Audiomixclient {
    static class Holder {
        static Audiomixclient instance;
    }
    static Audiomixclient getInstance(Context context) {
        synchronized (Audiomixclient.class) {
            if (Holder.instance == null) {
                Holder.instance = new Audiomixclient(context);
            }
            return Holder.instance;
        }
    }

    // Android likes to TAG each class for Logging
    private static final String TAG = Audiomixclient.class.getSimpleName();

    private static final int LISTEN_UDP_PORT = 13232;
    private static final int SEND_UDP_PORT = 13231;
    private static final String SERVER_IP = "10.0.0.4";    //.13
    private static final String EMULATOR_SERVER_IP = "10.0.2.2";
    private static final int SOCKET_TIMEOUT_MILLIS = 10;
    private static final int RETRIES = 10;
    DatagramSocket datagramSocket;
    final AtomicLong requestCount = new AtomicLong(System.currentTimeMillis());


    private Audiomixclient(Context context) {
        try {
            datagramSocket = new DatagramSocket(LISTEN_UDP_PORT);
            datagramSocket.setBroadcast(true);
            callServer(context, "reset", "");
        } catch (IOException e) {
            Log.e(TAG, "DatagramSocket failed " + LISTEN_UDP_PORT, e);
        }
    }

    InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp.ipAddress == 0) {
            // assume we are on the emulator
            return InetAddress.getByName(EMULATOR_SERVER_IP);
        }

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        return InetAddress.getByAddress(new byte[] {
                (byte)(broadcast >>> 24),
                (byte)(broadcast >>> 16),
                (byte)(broadcast >>> 8),
                (byte)broadcast});
    }

    public void callServer(Context context, final String command, final String path) {
        InetAddress address = null;
        try {
            address = getBroadcastAddress(context);
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
                    asyncCallServer(finalAddress, command, path);
                } catch (Exception e) {
                    Log.e(TAG, "could not call server with " + command, e);
                }
                return null;
            }
        }.execute();
    }

    private void asyncCallServer(InetAddress address, String command, String path) throws IOException  {
        StringBuilder builder = new StringBuilder();
        builder.append("audiomixclient/1 android 0\n")
                .append(requestCount.incrementAndGet())
                .append("\n").append(command).append("\n")
                .append(path).append("\n");
        byte[] bytes = builder.toString().getBytes();

        long start = System.currentTimeMillis();
        for (int attempt = 0; RETRIES > attempt; ++attempt) {
            Log.d(TAG, "Sending UDP packet " + builder + " to " + address + " from " + datagramSocket + " attempt " + attempt);
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
