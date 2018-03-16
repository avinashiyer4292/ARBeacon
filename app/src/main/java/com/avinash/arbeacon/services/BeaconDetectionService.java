package com.avinash.arbeacon.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.avinash.arbeacon.activities.CameraPreviewActivity2;
import com.avinash.arbeacon.activities.MainActivity;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by avinashiyer on 12/11/17.
 */

public class BeaconDetectionService extends Service {
    private static int BEACON_FOUND_FLAG = 0;
    private BeaconManager mBeaconManager;
    private static final String TAG ="BeaconDetectionService";
    private SharedPreferences appPrefs;
    private boolean isSameBeacon = false;

    public BeaconDetectionService(Context appContext){
        super();
        Log.d("Service","Service beacon detection started");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ServiceBeacon", "Beacon Service started!!!");
    }

    public BeaconDetectionService(){

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);
        //startTimer();
        mBeaconManager = new BeaconManager(getApplicationContext());
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady() {
                Log.d(TAG, "Starting beacon listening service...");
                mBeaconManager.startMonitoring(new BeaconRegion(
                        "region1",
                        UUID.fromString("914582A5-428C-4EAD-B509-E1DC63F54EB3"),686,6));
                mBeaconManager.startMonitoring(new BeaconRegion(
                        "region2",
                        UUID.fromString("914582A5-428C-4EAD-B509-E1DC63F54EB3"),686,7));
            }
        });
        mBeaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener(){
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                if(BEACON_FOUND_FLAG == 0){
                    Log.d(TAG, "Entered beacon region -- Size: "+beacons.size()+" "+beacons.get(0));
                    appPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    int beaconId = appPrefs.getInt("beacon",-1);
                    if(beaconId == beacons.get(0).getMinor() ){
                        Log.d(TAG, "Same beacon found");
                        isSameBeacon = true;
                    }
                    else{
                        Log.d(TAG, "Different beacon");
                        isSameBeacon = false;
                        appPrefs.edit().putInt("beacon", beacons.get(0).getMinor()).commit();
                        showNotification();
                    }
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
        return START_STICKY;
    }

    public void showNotification(){
        Intent notifyIntent = new Intent(this, CameraPreviewActivity2.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notifyIntent},
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Campus Tours!")
                .setContentText("You are near a point of interest!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
