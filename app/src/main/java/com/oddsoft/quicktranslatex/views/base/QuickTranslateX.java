package com.oddsoft.quicktranslatex.views.base;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.oddsoft.quicktranslatex.BuildConfig;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.utils.Constant;

import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by andycheng on 2015/6/28.
 */
public class QuickTranslateX extends Application {

    // Debugging switch
    public static final boolean APPDEBUG = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
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

    public enum TrackerName {
        APP_TRACKER // Tracker used only in this app.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            if (APPDEBUG) {
                analytics.getInstance(this).setDryRun(true);
            }
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(Constant.PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
}
