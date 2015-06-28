package com.oddsoft.quicktranslatex.app;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class JsonRequest extends JsonObjectRequest {

    public JsonRequest(int method
            , String url
            , JSONObject jsonRequest
            , Response.Listener<JSONObject> listener
            , Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    private Priority mPriority;

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    @Override
    public Priority getPriority() {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }

    @Override
    public String getBodyContentType() {
        //return super.getBodyContentType();
        return "application/x-www-form-urlencoded";
    }
}