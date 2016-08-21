package com.helioz.androiddrumclient;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    WifiManager.WifiLock wifiLock;

    private static final String SERVER_IP = "172.20.10.5";    //.13
    private static final String SERVER_PORT = "13231";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
/* https://developer.android.com/reference/android/os/StrictMode.html
        StrictMode is a developer tool which detects things you might be doing by accident and brings
        them to your attention so you can fix them.

        StrictMode is most commonly used to catch accidental disk or network access on the application's
        main thread, where UI operations are received and animations take place. Keeping disk and network
        operations off the main thread makes for much smoother, more responsive applications. By keeping
        your application's main thread responsive, you also prevent ANR dialogs from being shown to users.
*/
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);


        wifiLock = ((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, getClass().getCanonicalName());
        wifiLock.acquire();

        DrumView drumView = (DrumView) findViewById(R.id.drumView);

        drumView.setBackgroundColor(getResources().getColor(R.color.blue));

        String soundList = getSoundListFromServer();

    }

    String getSoundListFromServer() {
        String responseBody = "Got no text from server";
        URL url;
        HttpURLConnection connection;
        try {
            url = new URL("http://" + SERVER_IP + ":" + SERVER_PORT);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            responseBody = readStream(in);
            connection.disconnect();
        } catch (java.net.SocketTimeoutException error) {
            responseBody = "Connection timed out";
        } catch (MalformedURLException error){
            responseBody = "Malformed URL";
        } catch (ProtocolException error) {
            responseBody = "Protocol Exception";
        } catch (IOException error) {
            responseBody = "IOException";
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
