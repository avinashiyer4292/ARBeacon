package com.avinash.arbeacon.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.avinash.arbeacon.R;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Main Activity started");
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Searching for nearby beacons...");
        pDialog.setIndeterminate(false);
        pDialog.show();
        //checkBluetoothSupport(this, pDialog);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "Navigating to camera preview activity");
                Intent i =new Intent(MainActivity.this, CameraPreviewActivity.class);
                startActivity(i);
                finish();
            }
        },3000);
    }
}
