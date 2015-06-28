package com.oddsoft.quicktranslatex;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TranslateTask implements Runnable {
	private static final String TAG = "TranslateTask";
	private final MainActivity translate;
	private final String original, from, to;

	private String client_secret;
	private String client_id;

	TranslateTask(MainActivity translate, String original, String from, String to) {
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
		Log.d(TAG, client_secret);

		// Construct content
		String content = "grant_type=client_credentials";
		content += "&client_id="+client_id;
		content += "&client_secret=" + URLEncoder.encode(client_secret);
		content += "&scope=http://api.microsofttranslator.com";

		String q = original;

		try {
			// Check if task has been interrupted
			if (Thread.interrupted())
				throw new InterruptedException();

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

				Log.d(TAG, "access token: " + accessToken);
			}
			input.close();

			//Call Microsoft Translate
			HttpClient httpclient = new DefaultHttpClient();
			String urlStr = "http://api.microsofttranslator.com/v2/Http.svc/Translate?text="
					+ URLEncoder.encode(q)
					+ "&from=" + from
					+ "&to=" + to;
			urlStr = urlStr + "&appId=" + URLEncoder.encode("Bearer "+accessToken);
			HttpGet httpGet = new HttpGet(urlStr);
			HttpResponse response = httpclient.execute(httpGet);
			Log.d(TAG, "Response = " + response.getStatusLine().toString());
			HttpEntity entity = response.getEntity();

			if (response != null) {
				InputStream instream = entity.getContent();
				int l;
				Writer writer = new StringWriter();
				char[] buffer = new char[1024];
				Reader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
				while ((l = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, l);
				}

				Log.d(TAG, writer.toString());

				//print result
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(writer.toString())));
				result = doc.getFirstChild().getTextContent();

			}

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

}
