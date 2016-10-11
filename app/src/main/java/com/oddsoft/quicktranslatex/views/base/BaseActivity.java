package com.oddsoft.quicktranslatex.views.base;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.GoogleAnalytics;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by andycheng on 2016/10/11.
 */

public class BaseActivity  extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
