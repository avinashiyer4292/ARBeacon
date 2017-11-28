package com.avinash.arbeacon.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avinash.arbeacon.R;
import com.avinash.arbeacon.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;

public class CameraPreviewActivity2 extends AppCompatActivity implements SurfaceHolder.Callback,
        View.OnClickListener,
        SensorEventListener{
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false, language=false;
    LayoutInflater controlInflater = null;
    private TextView title,content;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    LatLng dest = new LatLng(29.615736, -82.374979);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview2);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        controlInflater = LayoutInflater.from(getBaseContext());
        final View viewControl = controlInflater.inflate(R.layout.camera_controls, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);


        this.addContentView(viewControl, layoutParamsControl);
        viewControl.setVisibility(View.GONE);

        title = (TextView)viewControl.findViewById(R.id.point_of_interest_title);
        content = (TextView)viewControl.findViewById(R.id.point_of_interest_content);
        title.setText(Constants.english_title_1);
        content.setText(Constants.english_content_1);

        FloatingActionButton audio_btn = (FloatingActionButton)viewControl.findViewById(R.id.point_of_interest_video),
                             video_btn = (FloatingActionButton)viewControl.findViewById(R.id.point_of_interest_audio),
                             language_btn = (FloatingActionButton)viewControl.findViewById(R.id.point_of_interest_language);


        audio_btn.setOnClickListener(this);
        video_btn.setOnClickListener(this);
        language_btn.setOnClickListener(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewControl.setVisibility(View.VISIBLE);
            }
        }, 3000);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.point_of_interest_audio: {}break;
            case R.id.point_of_interest_video: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/jOYbvKUopDM"));
                startActivity(browserIntent);
            }break;
            case R.id.point_of_interest_language: {
                if(language == false) {
                    title.setText(Constants.portugese_title_1);
                    content.setText(Constants.portugese_content_1);
                    language = true;
                }
                else
                {
                    title.setText(Constants.english_title_1);
                    content.setText(Constants.english_content_1);
                    language = false;
                }

            }break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }
}
