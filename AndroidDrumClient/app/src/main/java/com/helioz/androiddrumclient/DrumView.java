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


/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {
    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                Log.d("myTag", callServer());
            } catch (IOException error) {
                //error.printStackTrace(System.out);
                Log.d("myTag", "Failed to call server");
                throw new RuntimeException(error);
            }
        }
        return true;
    }

    private String callServer() throws IOException {
        URL url = new URL("http://www.google.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String responseBody;
        try {
            responseBody = readStream(in);
        } catch (IOException error) {
            //error.printStackTrace(System.out);
            responseBody = "Got no text from server";
            throw new RuntimeException(error);
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
