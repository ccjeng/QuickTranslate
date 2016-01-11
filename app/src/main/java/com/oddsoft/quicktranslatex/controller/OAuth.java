package com.oddsoft.quicktranslatex.controller;

import android.os.AsyncTask;
import android.util.Log;

import com.oddsoft.quicktranslatex.QuickTranslateX;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

/**
 * Created by andycheng on 2015/8/5.
 */
public class OAuth extends AsyncTask<String, Void, String> {
    private static final String TAG = "OAuth";

    public String getToken() {

        String[] aryClientSecret = {"aQpYdSsbH0hQZzj7KUEIwhZHnh4+NB5eMmMRkVoUW20="
                , "OyQFY5PhpxZNGIJlekizA786BVhufZumkVGuqubzPpI="
                , "gHaxgCWnUzhAtUuVXR5MKOJ2v+5cCAp7VlmmUN/NeFQ="
                , "NzUXAECywhvStqrpVyMoqyv8f8V7vqXBSNEGA3fDJR0="
                , "000VBs/Kx3C+oHQeZ5IzYKg4tiSECe4aiK78i2JKi2OI3M="};

        String[] aryClientId = {"android-oddsoft-quicktranslatexd"
                , "android-oddsoft-quicktranslatexd1"
                , "android-oddsoft-quicktranslatepro"
                , "android-oddsoft-quicktranslatexd2"
                , "android-oddsoft-quicktranslate"};


        int index = new Random().nextInt(aryClientSecret.length);

        String client_secret = aryClientSecret[index];
        String client_id = aryClientId[index];

        Log.d(TAG, "client_secret = " + client_secret);
        Log.d(TAG, "client_id = " + client_id);

        String authToken = "";

        // Construct content
        String content = "grant_type=client_credentials";
        content += "&client_id=" + client_id;
        content += "&client_secret=" + URLEncoder.encode(client_secret);
        content += "&scope=http://api.microsofttranslator.com";

        try {
            // Send data
            URL url = new URL("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13/");
            URLConnection conn = url.openConnection();

            // Let the run-time system (RTS) know that we want input.
            conn.setDoInput(true);
            // Let the RTS know that we want to do output.
            conn.setDoOutput(true);
            // No caching, we want the real thing.
            conn.setUseCaches(false);
            // Specify the content type.
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send POST output.
            DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
            printout.writeBytes(content);
            printout.flush();
            printout.close();

            // Get response data.
            DataInputStream input = new DataInputStream(conn.getInputStream());
            String str = "";

            while (null != ((str = input.readLine()))) {

                Log.d(TAG, "access token: " + str);

                JSONObject json = new JSONObject(str);
                authToken = json.get("access_token").toString();

                Log.d(TAG, "access token: " + authToken);
            }

            input.close();

        } catch (Exception e) {
            Log.e(TAG, "IOException", e);

        }

        return authToken;
    }

    @Override
    protected String doInBackground(String... params) {
        return getToken();
    }


    @Override
    protected void onPostExecute(String authToken) {
        super.onPostExecute(authToken);

        QuickTranslateX.setAuthState(true);
        QuickTranslateX.setAuthToken(authToken);

        Log.d(TAG, "authToken: " + authToken);
    }
}
