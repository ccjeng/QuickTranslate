package com.oddsoft.quicktranslatex.app;

import android.os.StrictMode;
import android.util.Log;

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
public class OAuth {
    private static final String TAG = "OAuth";

    private String client_secret;
    private String client_id;

    public void OAuth() {
    }

    public String getToken() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String[] aryClientSecret = {"aQpYdSsbH0hQZzj7KUEIwhZHnh4+NB5eMmMRkVoUW20="
                ,"OyQFY5PhpxZNGIJlekizA786BVhufZumkVGuqubzPpI="
                ,"gHaxgCWnUzhAtUuVXR5MKOJ2v+5cCAp7VlmmUN/NeFQ="};

        String[] aryClientId = {"android-oddsoft-quicktranslatexd"
                ,"android-oddsoft-quicktranslatexd1"
                ,"android-oddsoft-quicktranslatepro"};


        int index = new Random().nextInt(aryClientSecret.length);
        client_secret = aryClientSecret[index];
        client_id = aryClientId[index];


        Log.d(TAG, "client_secret = " + client_secret);
        Log.d(TAG, "client_id = " + client_id);

        String authToken = "";


        // Construct content
        String content = "grant_type=client_credentials";
        content += "&client_id="+client_id;
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

        } finally {
            //
        }



        return authToken;
    }
}
