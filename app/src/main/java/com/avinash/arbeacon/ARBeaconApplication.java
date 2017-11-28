package com.avinash.arbeacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.avinash.arbeacon.activities.MainActivity;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by avinashiyer on 11/6/17.
 */

public class ARBeaconApplication extends Application {
    private static final String TAG = "ARBeaconApplication";
    private static int BEACON_FOUND_FLAG = 0;
    private BeaconManager mBeaconManager;
    @Override
    public void onCreate() {
        super.onCreate();
        mBeaconManager = new BeaconManager(getApplicationContext());
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady() {
                Log.d(TAG, "Starting beacon listening service...");
                mBeaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("914582A5-428C-4EAD-B509-E1DC63F54EB3"),686,6));
            }
        });
        mBeaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener(){
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                if(BEACON_FOUND_FLAG == 0){
                    Log.d(TAG, "Entered beacon region "+beacons.get(0));
                    showNotification();
                    BEACON_FOUND_FLAG = 1;
                }

            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                if(BEACON_FOUND_FLAG == 1){
                    Log.d(TAG, "Exiting beacon region");
                    BEACON_FOUND_FLAG = 0;
                }

            }
        });
    }
    public void showNotification(){
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notifyIntent},
                                        PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                                        .setContentTitle("Beacon detected!")
                                        .setContentText("1 beacon found nearby")
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent)
                                        .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
