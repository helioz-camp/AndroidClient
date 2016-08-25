package com.helioz.androiddrumclient;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private static final int MAX_SIMULTANEOUS_PLAYBACK = 100;
    private static final int LISTEN_UDP_PORT = 13232;
    private static final int SEND_UDP_PORT = 13231;
    private static final String EMULATOR_SERVER_IP = "10.0.2.2";
    private static final int SOCKET_TIMEOUT_MILLIS = 10;
    private static final int RETRY_DELAY_MILLIS = 10;
    private static final int MAX_AGE_MILLIS = 100;
    DatagramSocket datagramSocket;
    final AtomicLong clientTokenNumber = new AtomicLong(System.currentTimeMillis());
    final Context context;
    private Handler backgroundHandler;
    private boolean isActivityScheduled = false;

    final ArrayList<PlaybackRequestState> repeatingPlaybacksWaitingToRetry = new ArrayList<>();

    Context getContext() { return context; }

    private Audiomixclient(Context context) {
        this.context = context;
        HandlerThread thread = new HandlerThread(Audiomixclient.class.getName());
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());

        try {
            datagramSocket = new DatagramSocket(LISTEN_UDP_PORT);
            datagramSocket.setBroadcast(true);
            sendMessage(new PlaybackRequestStateBuilder().build(),new Uri.Builder().path("reset").build());
        } catch (IOException e) {
            Log.e(TAG, "DatagramSocket failed " + LISTEN_UDP_PORT, e);
        }
    }

    private byte[] buildRequest(String token, Uri command) {
        StringBuilder builder = new StringBuilder();
        builder.append("audiomixclient/3 android 0\n")
                .append(token).append("\n")
                .append(command).append("\n");
        return builder.toString().getBytes();
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager)getContext().getSystemService(Context.WIFI_SERVICE);
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

    private String nextToken() {
        return String.valueOf(clientTokenNumber.incrementAndGet());
    }

    public PlaybackRequestState playSound(String sample) {
        PlaybackRequestState playback = new PlaybackRequestStateBuilder()
                .setSampleName(sample)
                .build();
        sendMessage(playback, new Uri.Builder().path("play")
                .appendQueryParameter("sample", playback.sampleName)
                .build());
        return playback;
    }

    static class PlaybackRequestState {
        String clientToken;
        String serverSequence;
        String sampleName;
        byte[] rawMessage;

        final StopWatch watch = new StopWatch();
        boolean repeatForever = false;
        String oldServerSequence = null;

        public PlaybackRequestState(String clientToken, String serverSequence, String sampleName) {
            this.clientToken = clientToken;
            this.serverSequence = serverSequence;
            this.sampleName = sampleName;
        }

        public void stopRepeating() {
            repeatForever = false;
        }

        @Override
        public String toString() {
            return "PlaybackRequestState{" +
                    "clientToken='" + clientToken + '\'' +
                    ", serverSequence='" + serverSequence + '\'' +
                    ", sampleName='" + sampleName + '\'' +
                    ", age=" + watch +
                    '}';
        }
    }

    void sendMessage(final PlaybackRequestState playback, Uri command) {
        playback.clientToken = nextToken();
        playback.rawMessage = buildRequest(playback.clientToken, command);
        playback.watch.resetWatch();
        backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sendAndWaitForServerReply(playback);
                } catch (Exception e) {
                    Log.e(TAG, "failed on " + playback, e);
                }
            }
        });
    }

    private void tryResend() {
        synchronized (repeatingPlaybacksWaitingToRetry) {
            for (PlaybackRequestState playback : repeatingPlaybacksWaitingToRetry) {
                if (!playback.repeatForever) {
                    continue;
                }
                sendMessage(playback, new Uri.Builder().path("queue")
                        .appendQueryParameter("sample", playback.sampleName)
                        .appendQueryParameter("sequence", playback.serverSequence)
                        .build());

            }
            repeatingPlaybacksWaitingToRetry.clear();
        }
    }


    public PlaybackRequestState repeatSound(String sample) {
        PlaybackRequestState playback = playSound(sample);
        playback.repeatForever = true;
        return playback;
    }

    public void stopSound(PlaybackRequestState state) {
        if (state == null) {
            return;
        }

        stopSequence(state.oldServerSequence);
        stopSequence(state.serverSequence);
    }

    private void stopSequence(String sequence) {
        if (sequence == null) {
            return;
        }
        PlaybackRequestState playback = new PlaybackRequestStateBuilder()
                .build();
        sendMessage(playback, new Uri.Builder().path("stop")
                .appendQueryParameter("sequence", sequence)
                .build());
    }

    private void sendAndWaitForServerReply(PlaybackRequestState playback) {
        byte[] recvBuf = new byte[1 << 16];
        sendPacketToServer(playback.rawMessage);
        for (int attempt = 0; MAX_AGE_MILLIS > playback.watch.ageMillis(); ++attempt) {

            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            try {
                datagramSocket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);
                datagramSocket.receive(receivePacket);
            } catch (SocketTimeoutException timeout) {
                sendPacketToServer(playback.rawMessage);
                continue;
            } catch (Exception e) {
                Log.e(TAG, "Could not contact server for " + playback + " at attempt " + attempt, e);
                maybeScheduleActivity();
                return;
            }

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
            String[] lines = response.split("\n");
            HashMap<String, String> keyValues = new HashMap<>();
            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("audiomixserver/")) {
                    continue;
                }
                int space = line.indexOf(' ');
                if (space < 0) {
                    keyValues.put(line, "");
                } else {
                    keyValues.put(line.substring(0, space), line.substring(space + 1));
                }
            }
            if (processResponse(playback, keyValues)) {
                maybeScheduleActivity();
                return;
            }
        }
        Log.w(TAG, "timed out trying to send " + playback);
        maybeScheduleActivity();
    }

    private boolean processResponse(PlaybackRequestState playback, Map<String, String> server) {
        String token = server.get("TOKEN");
        if (!playback.clientToken.equals(token)) {
            Log.w(TAG, "out of order response " + server + " for " + playback);
            return false;
        }
        Log.i(TAG, "received response to " + playback + ": " + server);
        String sequence = server.get("PLAYING");
        if (sequence == null) {
            sequence = server.get("QUEUED");
        }
        if (sequence != null) {
            playback.oldServerSequence = playback.serverSequence;
            playback.serverSequence = sequence;
        }

        if (playback.repeatForever) {
            synchronized (repeatingPlaybacksWaitingToRetry) {
                repeatingPlaybacksWaitingToRetry.add(playback);
            }
        }
        return true;
    }

    private void maybeScheduleActivity() {
        synchronized (repeatingPlaybacksWaitingToRetry) {
            if (!repeatingPlaybacksWaitingToRetry.isEmpty() && !isActivityScheduled) {
                backgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            tryResend();
                        } catch (Exception e) {
                            Log.w(TAG, "error in background processing", e);
                        }
                        isActivityScheduled = false;
                        maybeScheduleActivity();
                    }
                }, RETRY_DELAY_MILLIS);
            }
        }
    }



    private void sendPacketToServer(final byte[] msg) {
        InetAddress address = null;
        try {
            address = getBroadcastAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Log.d(TAG, "Sending UDP packet " + new String(msg, StandardCharsets.UTF_8) + " to " + address + " from " + datagramSocket);
            datagramSocket.send(new DatagramPacket(msg, msg.length, address, SEND_UDP_PORT));
        } catch (Exception e) {
            Log.e(TAG, "could not call server with " + new String(msg, StandardCharsets.UTF_8), e);
        }
    }

}
