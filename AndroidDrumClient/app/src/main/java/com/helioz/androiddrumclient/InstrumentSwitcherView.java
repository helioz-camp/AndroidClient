package com.helioz.androiddrumclient;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
//import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.view.MotionEvent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.ViewSwitcher;

import java.io.File;


/**
 * Created by jqjunk on 8/21/16.
 */


public class InstrumentSwitcherView extends LinearLayout {
    private DrumView targetDrumView;
    private TextSwitcher instrumentLabelSwitcher;
    private float x1,x2;
    static final double MIN_SWIPE_DISTANCE = 150.0;
    public String[] soundList;
    private int currentInstrumentIndex;
    private static final String TAG = "switcher view";
    private Animation slide_in_left;
    private Animation slide_in_right;
    private Animation slide_out_left;
    private Animation slide_out_right;

    public InstrumentSwitcherView (Context context, AttributeSet attrs) {
        super(context, attrs);

        //setting up button sub views
        InstrumentSwitchButton prevButton = new InstrumentSwitchButton(context, attrs, this, "previous");
        LinearLayout.LayoutParams prevParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.33f);
        //params.addRule(LinearLayout.ALIGN_PARENT_LEFT, LinearLayout.TRUE);
        prevButton.setBackgroundResource(R.color.black);
        prevButton.setText("< Previous");
        prevButton.setTextColor(Color.rgb(51,255,102));
        prevButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        prevButton.setPadding(20, 0, 0, 0);
        prevButton.setTextSize(20.0f);
        prevButton.setTypeface(Typeface.MONOSPACE);
        this.addView(prevButton, prevParams);

        instrumentLabelSwitcher = new TextSwitcher(context, attrs);
        setSwitcherFactoryOnSwitcher(instrumentLabelSwitcher, context, attrs);
        instrumentLabelSwitcher.setBackgroundColor(R.color.black);

        // Declare the in and out animations and initialize them
        slide_in_left = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        slide_in_right = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        slide_out_left = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
        slide_out_right = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);

        // set the animation type of textSwitcher
        instrumentLabelSwitcher.setInAnimation(slide_in_left);
        instrumentLabelSwitcher.setOutAnimation(slide_out_right);

        instrumentLabelSwitcher.setText("0"); // Default initial sound
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.34f);
        this.addView(instrumentLabelSwitcher, textViewParams);

        InstrumentSwitchButton nextButton = new InstrumentSwitchButton(context, attrs, this, "next");
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.33f);
        //params.addRule(LinearLayout.ALIGN_PARENT_LEFT, LinearLayout.TRUE);
        nextButton.setBackgroundResource(R.color.black);
        nextButton.setText("Next >");
        nextButton.setTextColor(Color.rgb(51,255,102));
        nextButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        nextButton.setPadding(0, 0, 20, 0);
        nextButton.setTextSize(20.0f);
        nextButton.setTypeface(Typeface.MONOSPACE);
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

        String soundName =  soundFilePathForIndex(index);
        targetDrumView.setCurrentSound(soundName);

        // set the animation type of textSwitcher
        instrumentLabelSwitcher.setInAnimation(slide_in_left);
        instrumentLabelSwitcher.setOutAnimation(slide_out_right);

        instrumentLabelSwitcher.setText(parseSoundName(soundName));

        return index;
    }

    //move down one place in the instrument list. If already at bottom place, wrap around to top
    private int changeInstrumentDown(int index) {
        if (index <= 0)
            index = soundList.length - 1;
        else
            index--;

        String soundName =  soundFilePathForIndex(index);
        targetDrumView.setCurrentSound(soundName);

        // set the animation type of textSwitcher
        instrumentLabelSwitcher.setInAnimation(slide_in_right);
        instrumentLabelSwitcher.setOutAnimation(slide_out_left);

        instrumentLabelSwitcher.setText(parseSoundName(soundName));

        return index;
    }

    private String soundFilePathForIndex(int index) {
        try {
            return soundList[index];
        } catch (Exception error) {
            return "0";
        }
    }

    private String parseSoundName(String soundPath) {
        int indexSlash = soundPath.lastIndexOf("/");
        if (indexSlash == -1) {
            return soundPath;
        }
        String fileName = soundPath.substring(indexSlash + 1, soundPath.length());
        int indexDot = fileName.lastIndexOf(".");
        if (indexDot == -1) {
            return fileName;
        }
        return fileName.substring(0, indexDot);
    }

    void setSwitcherFactoryOnSwitcher(TextSwitcher switcher, final Context context, final AttributeSet attrs) {
        switcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView instrumentLabel = new TextView(context, attrs);
                instrumentLabel.setBackgroundResource(R.color.black);
                instrumentLabel.setTextColor(Color.rgb(51,255,102));
                instrumentLabel.setGravity(Gravity.CENTER);
                instrumentLabel.setPadding(0, 0, 0, 0);
                instrumentLabel.setTextSize(20.0f);
                instrumentLabel.setTypeface(Typeface.MONOSPACE);

                return instrumentLabel;
            }
        });
    }
}
