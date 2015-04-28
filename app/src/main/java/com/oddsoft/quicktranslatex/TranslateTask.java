package com.oddsoft.quicktranslatex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import android.util.Log;

import org.json.JSONObject;

public class TranslateTask implements Runnable {
	private static final String TAG = "TranslateTask";
	private final QuickTranslate translate;
	private final String original, from, to;
	
	TranslateTask(QuickTranslate translate, String original, String from, String to) {
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
            String url = "http://frengly.com?src="+from+"&dest="+to+"&text"+q+"&email=ccjeng@gmail.com&password=ab1234&outformat=json";

            HttpURLConnection uc = (HttpURLConnection) new URL(url.toString()).openConnection();
            uc.setDoInput(true);
            uc.setDoOutput(true);

            try {
                Log.d(TAG, "getInputStream()");
                InputStream is= uc.getInputStream();

                JSONObject json = new JSONObject(toString(is));
                result = ((JSONObject)json.get("")).getString("translation");

            } finally { // http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
                uc.getInputStream().close();
                if (uc.getErrorStream() != null)
                    uc.getErrorStream().close();
            }

			//Translate.setKey("37A1C964DB8F38CA2505D785E33EA42946F49851");
			//Translate.setClientId("android-oddsoft-quicktranslate"/* Enter your Windows Azure Client Id here */);
		    //Translate.setClientSecret("VBs/Kx3C+oHQeZ5IzYKg4tiSECe4aiK78i2JKi2OI3M="/* Enter your Windows Azure Client Secret here */);
			
			if (to!=null) {
				//result = Translate.execute(q, Language.fromString(from), Language.fromString(to));
			}
			else {
				//auto detect 
				//result = Translate.execute(q, Language.fromString(to));
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

    private static String toString(InputStream inputStream) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();
        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while (null != (string = reader.readLine())) {
                    outputBuilder.append(string).append('\n');
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error reading translation stream.", ex);
        }
        return outputBuilder.toString();
    }
}
