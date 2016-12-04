package com.oddsoft.quicktranslatex.controller;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.oddsoft.quicktranslatex.R;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.utils.MD5;
import com.oddsoft.quicktranslatex.utils.Utils;
import com.oddsoft.quicktranslatex.views.MainActivity;
import com.oddsoft.quicktranslatex.views.base.QuickTranslateX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by andycheng on 2016/12/2.
 */

public class TranslateServiceBaidu {
    private static final String TAG = "TranslateServiceBaidu";

    private final MainActivity content;
    private final String query, from, to;

    private String result;
    private String appid;
    private String securityKey;

    public TranslateServiceBaidu(MainActivity content, String query, String from, String to) {
        this.content = content;
        this.query = query.replaceAll("(\\r|\\n|\\r\\n)+", "\\\n ");
        this.from = from;
        this.to = to;

        String[] appidArray = Constant.BAIDU_APPID.split(",");
        String[] securityKeyArray = Constant.BAIDU_KEY.split(",");
        int index = new Random().nextInt(appidArray.length);

        this.appid = appidArray[index];
        this.securityKey = securityKeyArray[index];


        try {
            String trans = doTranslate(query, from, to);
            if (trans.equals(""))
                trans = "..........";
            content.setTranslated(trans);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String doTranslate(String query, String from, String to) throws Exception {
        result = "";

        if (QuickTranslateX.APPDEBUG) {
            Log.d(TAG, "doTranslate(" + query + ", " + from + ", " + to + ")");
        }

        try {
            callTranslate();

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

    public void callTranslate() {

        String urlStr = getUrlWithQueryString(Constant.BAIDU_TRANS_API_HOST, buildParams());
        Log.d(TAG, urlStr);

        StringRequest request1 = new StringRequest(Request.Method.GET
                , urlStr
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    if (QuickTranslateX.APPDEBUG) {
                        Log.d(TAG, "Response = " + response);
                    }

                    result = parseJson(response);
                    content.setTranslated(result);


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

        VolleySingleton.getInstance(content).addToRequestQueue(request1);


    }

    private Map<String, String> buildParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }

    private static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(Utils.URLEncoder(value));

            i++;
        }

        return builder.toString();
    }


    private String parseJson(String json) {

        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray dataArray = jsonObject.getJSONArray("trans_result");

            String lineBreak = "";

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject c = dataArray.getJSONObject(i);

                if (!result.equals("")){
                    lineBreak ="\n";
                }
                result = result + lineBreak + c.getString("dst");
            }

            Log.d(TAG, "result = " + result);


        } catch (JSONException e) {

            result = parseError(json);
            e.printStackTrace();
        }


        return result;
    }


    private String parseError(String json) {
        String result = "";

        try {
            JSONObject jsonObject = new JSONObject(json);
            String errorCode = jsonObject.getString("error_code");
            String errorMsg = jsonObject.getString("error_msg");

            result = "(" + errorMsg + ")";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}