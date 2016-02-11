package com.oddsoft.quicktranslatex.controller;

import android.util.Log;
/*
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
*/
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.utils.SecretKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andycheng on 2016/1/23.
 */
public class Secret {
    public static final String TAG = "Secret";

    public static void execParseQuery() {
/*
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
            */
    }


    public static void execFirebase() {

        Firebase firebaseRef = new Firebase(Constant.FIREBASE_URL);

        Query queryRef = firebaseRef.child("key").orderByChild("active").equalTo(true);

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                ArrayList<String> clientList = new ArrayList<String>();
                ArrayList<String> secretList = new ArrayList<String>();
                String client = "";
                String secret = "";

                for (DataSnapshot keySnapshot : snapshot.getChildren()) {
                    SecretKey key = keySnapshot.getValue(SecretKey.class);

                    client = key.getClient();
                    secret = key.getSecret();
                    clientList.add(client);
                    secretList.add(secret);
                    //Log.d(TAG, key.getClient() + " - " + key.getSecret());
                }

                new OAuth(clientList, secretList).execute();

            }

            @Override
            public void onCancelled(FirebaseError error) {
                Log.d(TAG, "The read failed: " + error.getMessage());
            }
        });


    }
}
