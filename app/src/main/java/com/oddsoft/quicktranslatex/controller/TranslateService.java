package com.oddsoft.quicktranslatex.controller;

import java.io.StringReader;
import java.net.URLEncoder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.oddsoft.quicktranslatex.utils.Utils;
import com.oddsoft.quicktranslatex.views.MainActivity;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.QuickTranslateX;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TranslateService {
    private static final String TAG = "TranslateService";
    private final MainActivity content;
    private final String original, from, to;

    private String client_secret;
    private String client_id;

    private RequestQueue queue;

    private String result;

    private String authToken;
    private boolean isTokenValid = true;

    public TranslateService(MainActivity content, String original, String from, String to) {
        this.content = content;
        this.original = original;
        this.from = from;
        this.to = to;

        authToken = QuickTranslateX.getAuthToken();
        isTokenValid = QuickTranslateX.getAuthState();

        queue = Volley.newRequestQueue(content);

        try {
            String trans = doTranslate(original, from, to);
            if (trans.equals(""))
                trans = "..........";
            content.setTranslated(trans);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private String doTranslate(String original, String from, String to) throws Exception {
        result = ""; //translate.getResources().getString(R.string.translation_error);

        if (QuickTranslateX.APPDEBUG) {
            Log.d(TAG, "doTranslate(" + original + ", " + from + ", " + to + ")");
            Log.d(TAG, "isTokenValid = " + isTokenValid);
        }

        //String q = original;

        try {

            if (isTokenValid) {
                callTranslate(authToken);

            } else {
                //regenerate token
                Log.d(TAG, "regenerate token");

                if (Utils.isNetworkConnected(content)) {
                    Secret.execFirebase();
                    isTokenValid = true;
                    authToken = QuickTranslateX.getAuthToken();
                    isTokenValid = QuickTranslateX.getAuthState();

                } else {
                    result = content.getResources().getString(R.string.network_error);
                }

            }

        } catch (Exception e) {
            Log.e(TAG, "IOException", e);
            result = content.getResources().getString(R.string.translation_interrupted);
        } finally {
            //
        }

        // All done
        if (QuickTranslateX.APPDEBUG) {
            Log.d(TAG, " -> returned " + result);
        }
        return result;
    }


    private void callTranslate(String accessToken) {
        //Call Microsoft Translate
        String urlStr = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text="
                + URLEncoder.encode(original)
                + "&from=" + from
                + "&to=" + to;
        urlStr = urlStr + "&appId=" + URLEncoder.encode("Bearer "+accessToken);

        Log.d(TAG, urlStr);
        final StringRequest request1 = new StringRequest(Request.Method.GET
                , urlStr
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (QuickTranslateX.APPDEBUG) {
                        Log.d(TAG, "Response = " + response);
                    }
                    //check exception, if exception, set token=blank...
                    //<string xmlns="http://schemas.microsoft.com/2003/10/Serialization/">1</string>
                    //print result

                    if (response.substring(0,14).equals("<string xmlns=")) {
                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = db.parse(new InputSource(new StringReader(response)));
                        result = doc.getFirstChild().getTextContent();
                        content.setTranslated(result);
                    }
                    else {
                        isTokenValid = false;
                        QuickTranslateX.setAuthState(false);
                    }


                } catch (Exception e) {
                    Log.e(TAG, "onResponse error: " + e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.toString());
            }
        });

        //request.setPriority(Request.Priority.HIGH);
        queue.add(request1);

    }
}
