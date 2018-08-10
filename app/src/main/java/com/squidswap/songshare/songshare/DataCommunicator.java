package com.squidswap.songshare.songshare;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;



public class DataCommunicator {
    private FirebaseFirestore db;
    private JSONArray friends;

    public DataCommunicator(){
        this.db = FirebaseFirestore.getInstance();
        this.friends = new JSONArray();
    }

    //Pulls friends from the firebase database.
    public void queryFriends(){
        this.db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        friends.put(doc.getData());
                    }
                }
            }
        });
    }

    //Pulls all the shares from the firebase database.
    public void queryShares(){

    }

    public JSONArray getFriends(){
        return this.friends;
    }
}
