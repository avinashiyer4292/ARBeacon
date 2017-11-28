package com.avinash.arbeacon.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    public FirebaseInstanceIDService() {
        Log.d("Firebase instance", "instantiated firebase id");
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Firebase Instance ID","Refreshed Token: "+refreshedToken);

        sendTokenToAppServer(refreshedToken);
    }
    private void sendTokenToAppServer(String token){

    }
}
