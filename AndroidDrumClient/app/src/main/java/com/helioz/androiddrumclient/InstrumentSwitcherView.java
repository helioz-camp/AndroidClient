package com.helioz.androiddrumclient;

import android.content.Context;
import android.util.AttributeSet;
//import android.view.GestureDetector;
import android.widget.LinearLayout;
import android.view.MotionEvent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by jqjunk on 8/21/16.
 */


public class InstrumentSwitcherView extends LinearLayout {
    private DrumView targetDrumView;
    private float x1,x2;
    static final double MIN_SWIPE_DISTANCE = 150.0;
    public String[] soundList;
    private int currentInstrumentIndex;
    private static final String TAG = "switcher view";

    public InstrumentSwitcherView (Context context, AttributeSet attrs) {
        super(context, attrs);

        //setting up button sub views
        InstrumentSwitchButton prevButton = new InstrumentSwitchButton(context, attrs, this, "previous");
        LinearLayout.LayoutParams prevParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.33f);
        //params.addRule(LinearLayout.ALIGN_PARENT_LEFT, LinearLayout.TRUE);
        prevButton.setBackgroundResource(R.color.helioz);
        this.addView(prevButton, prevParams);

        TextView instrumentLabel = new TextView(context, attrs);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.34f);
        instrumentLabel.setBackgroundResource(R.color.black);
        instrumentLabel.setText(" HELIOZ ");
        instrumentLabel.setTextSize(20);
        this.addView(instrumentLabel, textViewParams);


        InstrumentSwitchButton nextButton = new InstrumentSwitchButton(context, attrs, this, "next");
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.33f);
        //params.addRule(LinearLayout.ALIGN_PARENT_LEFT, LinearLayout.TRUE);
        nextButton.setBackgroundResource(R.color.helioz);
        this.addView(nextButton, nextParams);
    }

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                return true;
            case MotionEvent.ACTION_UP:
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
    */

    public void setTargetDrumView (DrumView drumView) {
        targetDrumView = drumView;
    }

    public void nextInstrument() { currentInstrumentIndex = changeInstrumentUp(currentInstrumentIndex); }
    public void previousInstrument() { currentInstrumentIndex = changeInstrumentDown(currentInstrumentIndex); }


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
