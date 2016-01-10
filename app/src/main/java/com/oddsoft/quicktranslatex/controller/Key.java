package com.oddsoft.quicktranslatex.controller;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by andycheng on 2016/1/10.
 */
public class Key {
    private static final String TAG = "OAuth";

    private String clientID;
    private String clientSecure;

    List<String> clientIDList = new ArrayList<String>();
    List<String> clientSecureList = new ArrayList<String>();

    public void getKeyList(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("mstranslate");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> items, ParseException e) {
                if (e == null) {

                    int i = 0;
                    for (i = 0; i < items.size(); i++) {
                        clientIDList.add(items.get(i).get("client").toString());
                        clientSecureList.add(items.get(i).get("secret").toString());
                    }

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });

        if (clientIDList.size() > 0) {

            int index  = new Random().nextInt(clientIDList.size());
            setClientID(clientIDList.get(index));
            setClientSecure(clientSecureList.get(index));

        }

    }



    private void setClientID(String clientID) {
        this.clientID = clientID;
    }

    private void setClientSecure(String clientSecure) {
        this.clientSecure = clientSecure;
    }

    public String getClientID() {
        return clientID;
    }

    public String getClientSecure() {
        return clientSecure;
    }
}
