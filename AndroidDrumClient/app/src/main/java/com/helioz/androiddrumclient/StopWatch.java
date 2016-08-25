package com.helioz.androiddrumclient;

/**
 * Created by john on 8/24/16.
 */
public class StopWatch {
    long start = System.currentTimeMillis();

    public void resetWatch() {
        start = System.currentTimeMillis();
    }

    long ageMillis() {
        return System.currentTimeMillis() - start;
    }

    public String toString() {
        return ageMillis() + "ms";
    }


}
