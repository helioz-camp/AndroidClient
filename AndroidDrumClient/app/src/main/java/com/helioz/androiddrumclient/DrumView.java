package com.helioz.androiddrumclient;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;

/**
 * Created by jqjunk on 7/24/16.
 */
public class DrumView extends View {
    public DrumView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN)
            Log.d("myTag", "This is my message");
        return true;
    }
}
