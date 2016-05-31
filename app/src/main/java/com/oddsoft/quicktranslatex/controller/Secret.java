package com.oddsoft.quicktranslatex.controller;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.oddsoft.quicktranslatex.utils.Constant;
import com.oddsoft.quicktranslatex.utils.SecretKey;

import java.util.ArrayList;

/**
 * Created by andycheng on 2016/1/23.
 */
public class Secret {
    public static final String TAG = "Secret";


    public static void execFirebase() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constant.FIREBASE_URL);

        Query queryRef = ref.child("key").orderByChild("active").equalTo(true);

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
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "The read failed: " + error.getMessage());
            }
        });


    }
}
