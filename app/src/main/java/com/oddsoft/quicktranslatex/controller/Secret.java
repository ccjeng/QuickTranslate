package com.oddsoft.quicktranslatex.controller;

import android.util.Log;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.utils.SecretKey;

import java.util.ArrayList;

/**
 * Created by andycheng on 2016/1/23.
 */
public class Secret {
    public static final String TAG = "Secret";


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
