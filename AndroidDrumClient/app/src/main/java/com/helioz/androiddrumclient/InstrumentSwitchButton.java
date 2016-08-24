package com.helioz.androiddrumclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by jqjunk on 8/23/16.
 */
public class InstrumentSwitchButton extends TextView {
    private InstrumentSwitcherView parentInstrumentSwitcherView;
    private String buttonType;

    public InstrumentSwitchButton (Context context, AttributeSet attrs, InstrumentSwitcherView instrumentSwitcherView, String typeOfButton) {
        super(context, attrs);
        parentInstrumentSwitcherView = instrumentSwitcherView;
        buttonType = typeOfButton;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                if (buttonType.equals("next"))
                    parentInstrumentSwitcherView.nextInstrument();
                else
                    parentInstrumentSwitcherView.previousInstrument();
                break;
        }
        return super.onTouchEvent(event);
    }


}
