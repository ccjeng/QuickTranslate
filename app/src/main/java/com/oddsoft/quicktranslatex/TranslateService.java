package com.oddsoft.quicktranslatex;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.os.StrictMode;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.oddsoft.quicktranslatex.app.JsonRequest;
import com.oddsoft.quicktranslatex.app.PostRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TranslateService {
    private static final String TAG = "TranslateTask";
    private final MainActivity translate;
    private final String original, from, to;

    private String client_secret;
    private String client_id;

    private RequestQueue queue;

    private String result;

    TranslateService(MainActivity translate, String original, String from, String to) {
        this.translate = translate;
        this.original = original;
        this.from = from;
        this.to = to;

        String[] aryClientSecret = {"aQpYdSsbH0hQZzj7KUEIwhZHnh4+NB5eMmMRkVoUW20="
                ,"OyQFY5PhpxZNGIJlekizA786BVhufZumkVGuqubzPpI="
                ,"gHaxgCWnUzhAtUuVXR5MKOJ2v+5cCAp7VlmmUN/NeFQ="};

        String[] aryClientId = {"android-oddsoft-quicktranslatexd"
                ,"android-oddsoft-quicktranslatexd1"
                ,"android-oddsoft-quicktranslatepro"};

        int index = new Random().nextInt(aryClientSecret.length);
        client_secret = aryClientSecret[index];
        client_id = aryClientId[index];

        queue = Volley.newRequestQueue(translate);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            String trans = doTranslate(original, from, to);
            if (trans.equals(""))
                trans = "..........";
            translate.setTranslated(trans);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    private String doTranslate(String original, String from, String to) throws Exception {
        result = ""; //translate.getResources().getString(R.string.translation_error);
        Log.d(TAG, "doTranslate(" + original + ", " + from + ", "	+ to + ")");
        Log.d(TAG, client_secret);


        String q = original;

        try {
            // Check if task has been interrupted
            //if (Thread.interrupted())
            //    throw new InterruptedException();

            // Construct content
            String content = "grant_type=client_credentials";
            content += "&client_id="+client_id;
            content += "&client_secret=" + URLEncoder.encode(client_secret);
            content += "&scope=http://api.microsofttranslator.com";

/*
            JSONObject params = new JSONObject();

            try {
                params.put("client_id", client_id);
                params.put("client_secret", client_secret);
                params.put("scope", "http://api.microsofttranslator.com" );
                params.put("grant_type", "client_credentials" );
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
*/

            //Map<String,String> params = new HashMap<String, String>();
/*
            JSONObject params = new JSONObject();
            params.put("client_id", client_id);
            params.put("client_secret", client_secret);
            params.put("scope", "http://api.microsofttranslator.com");
            params.put("grant_type", "client_credentials");

            StringRequest request = new StringRequest(Request.Method.POST
                    , "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13?" + content
                    //, null
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        Log.d(TAG, "access token: " + response.toString());
                        //callTranslate(response.getString("access_token").toString());


                    } catch (Exception e) {
                        Log.e(TAG, "onResponse=" + e.toString());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "onErrorResponse=" + error.toString());
                }
            }){


                @Override
                protected Map<String,String> getParams() throws AuthFailureError{
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("client_id",client_id);
                    params.put("client_secret", URLEncoder.encode(client_secret));
                    params.put("scope","http://api.microsofttranslator.com");
                    params.put("grant_type","client_credentials");

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    //Map<String, String> params = super.getHeaders();
                    //if(params == null)
                    //    params =new HashMap<String, String>();
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    return params;
                }

            };

            //request.setPriority(Request.Priority.HIGH);
            queue.add(request);
*/

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
			int start = 0;
			int end = 0;
			String accessToken = "";


			while (null != ((str = input.readLine()))) {
				Log.d(TAG, "access token: " + str);

				JSONObject json = new JSONObject(str);
				accessToken = json.get("access_token").toString();

                callTranslate(accessToken);
				Log.d(TAG, "access token: " + accessToken);
			}
			input.close();



        } catch (Exception e) {
            Log.e(TAG, "IOException", e);
            result = translate.getResources().getString(
                    R.string.translation_interrupted);
	   /*  } catch (InterruptedException e) {
			Log.d(TAG, "InterruptedException", e);
				result = translate.getResources().getString(
						R.string.translation_interrupted);
						*/
        } finally {
            //
        }

        // All done
        Log.d(TAG, " -> returned " + result);
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
                    Log.d(TAG, "Response = " + response.toString());

                    //print result
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = db.parse(new InputSource(new StringReader(response.toString())));
                    result = doc.getFirstChild().getTextContent();
                    translate.setTranslated(result);

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        //request.setPriority(Request.Priority.HIGH);
        queue.add(request1);

    }
}
