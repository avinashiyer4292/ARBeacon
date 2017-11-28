package com.avinash.arbeacon.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public MyFirebaseMessagingService() {
        Log.d("Firebase messaging", "instantiated firebase messaging service");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("Messaging service", remoteMessage.getFrom()+" - "+remoteMessage.getNotification().getBody());
    }
}
