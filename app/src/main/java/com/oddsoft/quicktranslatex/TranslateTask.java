package com.oddsoft.quicktranslatex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import android.util.Log;

import org.json.JSONObject;

public class TranslateTask implements Runnable {
	private static final String TAG = "TranslateTask";
	private final MainActivity translate;
	private final String original, from, to;
	
	TranslateTask(MainActivity translate, String original, String from, String to) {
		this.translate = translate;
		this.original = original;
		this.from = from;
		this.to = to;
	}
	
	public void run() {
		// Translate the original text to the target language
		String trans;
		try {
			trans = doTranslate(original, from, to);
			translate.setTranslated(trans);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	* Call the Google Translation API to translate a string from one
	* language to another. For more info on the API see:
	* http://code.google.com/apis/ajaxlanguage
	*/
	/**
	 * @param original
	 * @param from
	 * @param to
	 * @return
	 * @throws Exception 
	 */
	private String doTranslate(String original, String from, String to) throws Exception {
		String result = translate.getResources().getString(R.string.translation_error);
		Log.d(TAG, "doTranslate(" + original + ", " + from + ", "	+ to + ")");

		try {
			// Check if task has been interrupted
			if (Thread.interrupted())
				throw new InterruptedException();

			String q = URLEncoder.encode(original, "UTF-8");

            //http://frengly.com?src=fr&dest=en&text=Bonjour+monsieur&email=ccjeng@gmail.com&password=ab1234&outformat=json
            String url = "http://frengly.com?src="+from+"&dest="+to+"&text="+q+"&email=ccjeng@gmail.com&password=ab1234&outformat=json";

            Log.d(TAG, url);

            InputStream is= new URL(url).openStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

                String jsonText = readAll(rd);
                JSONObject json = new JSONObject(jsonText);

                Log.d(TAG, "json = " + json);
                result = json.get("translation").toString();

            } finally { // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
                is.close();
            }

			// Check if task has been interrupted
			if (Thread.interrupted())
				throw new InterruptedException();
	     } catch (IOException e) {
				Log.e(TAG, "IOException", e);
	     } catch (InterruptedException e) {
			Log.d(TAG, "InterruptedException", e);
				result = translate.getResources().getString(
						R.string.translation_interrupted);
	     } finally {
	    	 //
			}
     			
		// All done
		Log.d(TAG, " -> returned " + result);
		return result;
	}

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
