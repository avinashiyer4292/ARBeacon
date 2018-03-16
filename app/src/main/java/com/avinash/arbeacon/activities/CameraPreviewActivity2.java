package com.avinash.arbeacon.activities;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import com.github.clans.fab.FloatingActionButton;

import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avinash.arbeacon.R;
import com.avinash.arbeacon.utils.Constants;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CameraPreviewActivity2 extends AppCompatActivity implements SurfaceHolder.Callback,
        View.OnClickListener,
        SensorEventListener,
        TextToSpeech.OnInitListener{
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false, language=false;
    LayoutInflater controlInflater = null;
    private TextView title,content;
    // record the compass picture angle turned
    private float currentDegree = 0f, bearing;
    private SensorManager mSensorManager;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private int mStatus = 0, languageStatus=-1;
    private TextToSpeech tts;
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton audio_btn;
    View viewControl;
    private MediaPlayer mMediaPlayer;
    private boolean mSpeechTextProcessed = false,
                    shownAudioProgressDialog=false,
                    isCameraPreview = true,
                    isFontBigger = false;
    private final String FILENAME = "/wpta_tts.wav";
    private ProgressDialog pDialog;
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
        viewControl = controlInflater.inflate(R.layout.camera_controls, null);
        ViewGroup.LayoutParams layoutParamsControl
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);


        this.addContentView(viewControl, layoutParamsControl);
        viewControl.setVisibility(View.GONE);

        //get bearing from MapsActivity
        Bundle extras = getIntent().getExtras();
        bearing = extras.getFloat("bearing");

        title = (TextView)viewControl.findViewById(R.id.point_of_interest_title);
        content = (TextView)viewControl.findViewById(R.id.point_of_interest_content);
        title.setText(Constants.english_title_1);
        content.setText(Constants.english_content_1);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        materialDesignFAM.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (materialDesignFAM.isOpened()) {
                   //
                    //Toast.makeText(CameraPreviewActivity2.this,"svsv",Toast.LENGTH_SHORT).show();
                    materialDesignFAM.close(true);
                }
                else
                    materialDesignFAM.open(true);
            }
        });
        FloatingActionButton video_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_video),
                            language_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_language),
                            contrast_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_contrast),
                            text_size_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_text_size),
                            toggle_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_toggle);

        audio_btn = (FloatingActionButton) findViewById(R.id.point_of_interest_audio);
        audio_btn.setOnClickListener(this);
        video_btn.setOnClickListener(this);
        language_btn.setOnClickListener(this);
        contrast_btn.setOnClickListener(this);
        text_size_btn.setOnClickListener(this);
        toggle_btn.setOnClickListener(this);

        tts = new TextToSpeech(this, this);
        // Creating an instance of MediaPlayer
        mMediaPlayer = new MediaPlayer();

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        // Stop the MediaPlayer
        mMediaPlayer.stop();

        // Release the MediaPlayer
        mMediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        mStatus=status;
        Locale l=null;
        switch(languageStatus){
            case 0: l= new Locale("en"); break;
            case 1: l = new Locale("pt"); break;
            case 2: l = new Locale("es","ES"); break;
            case 3: l = new Locale("fr");break;
        }
        //tts.setLanguage(l);
        setTts(tts,l);
    }
    @Override
    public void onClick(View view) {
        if(view.getId()!=R.id.point_of_interest_audio || view.getId()!=R.id.point_of_interest_text_size)
            closeFabMenu();
        switch(view.getId()){
            case R.id.point_of_interest_audio: {
                playAudio();
            }break;
            case R.id.point_of_interest_video: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/jOYbvKUopDM"));
                startActivity(browserIntent);
            }break;
            case R.id.point_of_interest_language: {
                showLanguageDialog();
            }break;
            case R.id.point_of_interest_contrast:{
                if(isCameraPreview){
                    surfaceView.setVisibility(View.INVISIBLE);
                    title.setTextColor(Color.BLACK);
                    content.setTextColor(Color.BLACK);
                    isCameraPreview = false;
                }
                else{
                    surfaceView.setVisibility(View.VISIBLE);
                    title.setTextColor(Color.WHITE);
                    content.setTextColor(Color.WHITE);
                    isCameraPreview = true;
                }
            }break;
            case R.id.point_of_interest_text_size:{
                if(!isFontBigger){
                    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()+12);
                    content.setTextSize(TypedValue.COMPLEX_UNIT_PX, content.getTextSize()+12);
                    isFontBigger = true;
                }
                else
                {
                    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()-12);
                    content.setTextSize(TypedValue.COMPLEX_UNIT_PX, content.getTextSize()-12);
                    isFontBigger = false;
                }
            }break;
            case R.id.point_of_interest_toggle:{
                Intent i=new Intent(CameraPreviewActivity2.this, MapsActivity.class);
                startActivity(i);
                finish();
            }break;
        }
    }

    private void showLanguageDialog(){
        AlertDialog.Builder dialogBuilder  = new AlertDialog.Builder(CameraPreviewActivity2.this);
        LayoutInflater inflater = CameraPreviewActivity2.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_language_dialog, null);
        dialogBuilder.setView(dialogView);
        final ListView listView = (ListView) dialogView.findViewById(R.id.languageListview);
        // Defined Array values to show in ListView
        String[] values = new String[] { "English",
                "Portugese",
                "Spanish",
                "French",
                "German"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);

        final AlertDialog alertDialog = dialogBuilder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition     = position;
                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);
                // Show Alert
//                Toast.makeText(getApplicationContext(),
//                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
//                        .show();
                if(position==0){
                    languageStatus = 0;
                    title.setText(Constants.english_title_1);
                    content.setText(Constants.english_content_1);
                }
                else if(position==1){
                    languageStatus = 1;
                    title.setText(Constants.portugese_title_1);
                    content.setText(Constants.portugese_content_1);
                }
                else if(position==2){
                    languageStatus = 2;
                    title.setText(Constants.spanish_title_1);
                    content.setText(Constants.spanish_content_1);
                }
                else if(position==3) {
                    languageStatus = 3;
                    title.setText(Constants.french_title_1);
                    content.setText(Constants.french_content_1);
                }
                alertDialog.dismiss();
            }
        });

//        dialogBuilder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int position) {
//                alertDialog.dismiss();
//            }
//        });
        alertDialog.show();
    }

    private void playAudio(){

        if(!shownAudioProgressDialog) {
            pDialog = new ProgressDialog(CameraPreviewActivity2.this);
            pDialog.setIndeterminate(false);
            pDialog.setMessage("Waiting for audio....");
            pDialog.show();
            shownAudioProgressDialog=true;
        }
        Log.d("AUDIO BUTTON", "Text to speech will get called");
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            playMediaPlayer(1);
            return;
        }
        HashMap<String, String> myHashRender = new HashMap();
        String utteranceID = "wpta";
        myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + FILENAME;
        Log.d("AUDIO BUTTON", "Filename: "+FILENAME);
        if(!mSpeechTextProcessed){
            String textToProcess = title.getText().toString()+"."+content.getText().toString();
            int status = tts.synthesizeToFile(textToProcess, myHashRender, fileName);
        }else {
            if (pDialog != null)
                pDialog.dismiss();
            Log.d("AUDIO BUTTON", "playing audio player....");
            playMediaPlayer(0);
        }
    }

    private void closeFabMenu(){
        if(materialDesignFAM.isOpened())
            materialDesignFAM.close(true);
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

        //Log.d("Bearing in camera", String.valueOf(bearing));
        float degree = Math.round(sensorEvent.values[0]);
        //String logm = ""+Float.toString(degree)+ " degrees";
        //Log.d("Heading:", String.valueOf(degree) );
        float bh, bl;
        bl = (bearing-45)>0?bearing-45:315+bearing;
        bh = (bearing+45)<360?bearing+45:bearing-315;
        //Log.d("values","high: "+bh+" low: "+bl);
        if((degree<bh && degree>bl && bl<bh)  )
            viewControl.setVisibility(View.VISIBLE);
        else if( bl>bh && ((degree>bl &&degree<360)||(degree>0&&degree<bh)))
            viewControl.setVisibility(View.VISIBLE);
        else
            viewControl.setVisibility(View.GONE);
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


    //tts code
    @SuppressWarnings("deprecation")
    @TargetApi(15)
    public void setTts(TextToSpeech tts, Locale l) {
        this.tts = tts;
        //Toast.makeText(CameraPreviewActivity2.this,"laNGUAGE: "+languageStatus,Toast.LENGTH_SHORT).show();
        this.tts.setLanguage(l);
        if (Build.VERSION.SDK_INT >= 15) {
            this.tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    // Speech file is created
                    mSpeechTextProcessed = true;
                    Log.d("AUdio spoken","Utterance completed!");
                    // Initializes Media Player
                    initializeMediaPlayer();
                    // Start Playing Speech
                    playMediaPlayer(0);
                    shownAudioProgressDialog = false;
                }
                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {
            this.tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    // Speech file is created
                    mSpeechTextProcessed = true;
                    Log.d("AUdio spoken","Utterance completed!");
                    // Initializes Media Player
                    initializeMediaPlayer();
                    // Start Playing Speech
                    playMediaPlayer(0);
                    shownAudioProgressDialog = false;
                }
            });
        }
    }
        private void initializeMediaPlayer(){
            Log.d("AUDIO BUTTON", "Intialize media player");
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + FILENAME;
            Uri uri  = Uri.parse("file://"+fileName);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(getApplicationContext(), uri);
                mMediaPlayer.prepare();
                if(pDialog!=null)
                    pDialog.dismiss();
                Toast.makeText(CameraPreviewActivity2.this,"Click the audio button again to play audio", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void playMediaPlayer(int status){
            // Start Playing
            if(status==0){
                audio_btn.setImageResource(R.drawable.ic_pause_black_24dp);
                //audioPlayBtn.setImageResource(android.R.drawable.ic_media_pause);
                mMediaPlayer.start();
            }

            // Pause Playing
            if(status==1){
                audio_btn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                //audioPlayBtn.setImageResource(android.R.drawable.ic_media_play);
                mMediaPlayer.pause();
            }
        }


    }


