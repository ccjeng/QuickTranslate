package com.oddsoft.quicktranslatex.app;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class PostRequest extends Request {

    Map<String, String> params;
    private Response.Listener listener;


    public PostRequest(int method
            , String url
            , Map<String, String> params
            , Response.Listener<JSONObject> listener
            , Response.ErrorListener errorListener) {
        //super(method, url, jsonRequest, listener, errorListener);

        super(method, url, errorListener);
        this.params = params;
        this.listener = listener;
    }

    private Priority mPriority;

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    @Override
    protected void deliverResponse(Object response) {
        listener.onResponse(response);

    }

   // @Override
   // public Map<String, String> getParams() throws AuthFailureError {
   //     return params;
   // }

    @Override
    public String getBodyContentType() {
        //return super.getBodyContentType();
        return "application/x-www-form-urlencoded";
    }

    @Override
    public Priority getPriority() {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }


    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.d(QuickTranslateX.TAG,jsonString);
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}