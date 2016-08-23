package com.helioz.androiddrumclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.widget.TextView;
import com.helioz.androiddrumclient.DrumView;
import android.view.MotionEvent;
import android.util.Log;


/**
 * Created by jqjunk on 8/21/16.
 */


public class InstrumentSwitcherView extends TextView {
    private DrumView targetDrumView;
    private float x1,x2;
    static final double MIN_SWIPE_DISTANCE = 150.0;
    public String[] soundList;
    private String currentInstrumentPath;
    private int currentInstrumentIndex;
    private static final String TAG = "switcher view";

    public InstrumentSwitcherView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "switcher was touched down");
                x1 = event.getX();
                return true;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "switcher was touched up");
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                    if (deltaX > 0)
                        currentInstrumentIndex = changeInstrumentUp(currentInstrumentIndex);
                    else
                        currentInstrumentIndex = changeInstrumentDown(currentInstrumentIndex);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setTargetDrumView (DrumView drumView) {
        targetDrumView = drumView;
    }

    //move up one place in the instrument list. If already at top place, wrap around to bottom
    private int changeInstrumentUp(int index) {
        if (index < soundList.length - 1)
            index++;
        else
            index = 0;

        try {
            targetDrumView.setCurrentSound(soundList[index]);
        } catch (Exception error) {
            targetDrumView.setCurrentSound("0");
        }

        return index;
    }

    //move down one place in the instrument list. If already at bottom place, wrap around to top
    private int changeInstrumentDown(int index) {
        if (index <= 0)
            index = soundList.length - 1;
        else
            index--;

        try {
            targetDrumView.setCurrentSound(soundList[index]);
        } catch (Exception error) {
            targetDrumView.setCurrentSound("0");
        }
        return index;
    }

}
