package com.oddsoft.quicktranslatex;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;

import java.util.HashMap;

/**
 * Created by andycheng on 2015/6/28.
 */
public class QuickTranslateX extends Application {

    // Debugging switch
    public static final boolean APPDEBUG = true;
    public static final String TAG = QuickTranslateX.class.getSimpleName();
    //Parse
    private static final String PARSE_APPLICATION_ID = "qW64sEj83siCgiwUrFGEcxUzhlMA2YU5Zc75I8mq";
    private static final String PARSE_CLIENT_KEY = "LU3xFlX46XaIl2JKba7HlmclWv0q1tbxQ5mLMlna";


    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);
    }

    private static Boolean mAuthState = false;
    public static Boolean getAuthState(){
        return mAuthState;
    }
    public static void setAuthState(Boolean s){
        mAuthState = s;
    }

    private static String mAuthToken = "";
    public static String getAuthToken(){
        return mAuthToken;
    }
    public static void setAuthToken(String s){
        mAuthToken = s;
    }

    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-19743390-11";
    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            //analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            if (APPDEBUG) {
                analytics.getInstance(this).setDryRun(true);
            }
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}
