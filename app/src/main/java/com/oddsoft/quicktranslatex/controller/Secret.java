package com.oddsoft.quicktranslatex.controller;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andycheng on 2016/1/23.
 */
public class Secret {
    public static final String TAG = "Utils";

    public static void execParseQuery() {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("mstranslate");

        query.whereEqualTo("active", true);

        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> items, ParseException e) {

                ArrayList<String> clientList = new ArrayList<String>();
                ArrayList<String> secretList = new ArrayList<String>();
                String client = "";
                String secret = "";

                if (e == null) {
                    int i = 0;
                    for (i = 0; i < items.size(); i++) {

                        client = items.get(i).get("client").toString();
                        secret = items.get(i).get("secret").toString();
                        clientList.add(client);
                        secretList.add(secret);

                        Log.d(TAG, client + " # " + secret);
                    }

                    new OAuth(clientList, secretList).execute();

                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }

            }


        });
    }
}
