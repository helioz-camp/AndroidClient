package com.helioz.androiddrumclient;

import android.view.ViewGroup;
import android.view.View;
import android.util.AttributeSet;
import android.content.Context;
import android.view.LayoutInflater;
import com.helioz.androiddrumclient.DrumView;


/**
 * Created by jqjunk on 8/18/16.
 */
public class DrumViewGroup extends ViewGroup {
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public DrumViewGroup (Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.drum_view_group, this, true);

        View myView = getChildAt(0);
    }

}
