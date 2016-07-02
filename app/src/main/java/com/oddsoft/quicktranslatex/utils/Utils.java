package com.oddsoft.quicktranslatex.utils;

/**
 * Created by andycheng on 2015/8/19.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Utils {
    public static final String TAG = "Utils";
    private static String mLastVersionRun;
    private static SharedPreferences prefs = null;

    public static void setLastVersionRun(String lastVersionRun) {
        put("LastVersionRun", lastVersionRun);
        Utils.mLastVersionRun = lastVersionRun;
    }

    public static String getLastVersionRun() {
        return mLastVersionRun;
    }

    public static boolean newVersionInstalled(Activity a) {
        if (prefs==null)
            prefs = PreferenceManager.getDefaultSharedPreferences(a);
        String thisVersion = getVersion(a);
        Utils.mLastVersionRun = prefs.getString("LastVersionRun", "");
        String lastVersionRun = mLastVersionRun;

        setLastVersionRun(thisVersion);
        if (thisVersion.equals(lastVersionRun)) {
            return false;
        } else {
            return true;
        }
    }


    public static boolean isNewInstallation(Activity a) {
        if (prefs==null)
            prefs = PreferenceManager.getDefaultSharedPreferences(a);
        if (prefs.getString("LastVersionRun", "").equals("")) {
            setLastVersionRun(getVersion(a));
            return true;
        } else
            return false;
    }
    /**
     * Retrieves the packaged version of the application
     *
     * @param a
     *            - The Activity to retrieve the current version
     * @return the version-string
     */
    public static String getVersion(Activity a) {
        String result = "";
        try {
            PackageManager manager = a.getPackageManager();
            PackageInfo info = manager.getPackageInfo(a.getPackageName(), 0);
            result = info.versionName;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Unable to get application version: " + e.getMessage());
            result = "";
        }
        return result;
    }

    private static void put(String constant, Object o) {
        SharedPreferences.Editor editor = prefs.edit();
        if (o instanceof String) {
            editor.putString(constant, (String) o);
        } else if (o instanceof Integer) {
            editor.putInt(constant, (Integer) o);
        } else if (o instanceof Long) {
            editor.putLong(constant, (Long) o);
        } else if (o instanceof Boolean) {
            editor.putBoolean(constant, (Boolean) o);
        }
        editor.apply();
    }


    private static ConnectivityManager connMgr;

    public static boolean isNetworkConnected(Context context){
        if(null == connMgr){
            connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }

    }


    public static String URLEncoder(String input) {
        String encoded_input = input;

        try {
            encoded_input = URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encoded_input;

    }

}
