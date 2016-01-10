package com.oddsoft.quicktranslatex.views;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.oddsoft.quicktranslatex.R;

/**
 * Created by andycheng on 2016/1/10.
 */
public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
