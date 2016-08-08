package com.helioz.androiddrumclient;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;

public class MainActivity extends AppCompatActivity {
    WifiManager.WifiLock wifiLock;

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
    }
}
