package com.oddsoft.quicktranslatex.utils;

import android.app.Activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.oddsoft.quicktranslatex.QuickTranslateX;

/**
 * Created by andycheng on 2015/6/28.
 */
public class Analytics {
    public static void trackerPage(Activity activity) {
        Tracker t = ((QuickTranslateX) activity.getApplication()).getTracker(
                QuickTranslateX.TrackerName.APP_TRACKER);
        t.setScreenName(activity.getClass().getSimpleName());
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    public static void trackEvent(Activity activity
            , String category, String action, String label, long value) {
        Tracker t = ((QuickTranslateX) activity.getApplication()).getTracker(
                QuickTranslateX.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }
}
