package com.avinash.arbeacon.activities;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.avinash.arbeacon.R;

import java.util.List;
import java.util.Random;

public class CameraPreviewActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera mCamera;
    SurfaceView mPreview;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    LayoutInflater controlInflater;
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mPreview.getHolder());

            //Canvas canvas = surfaceHolder.lockCanvas();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height1 = displayMetrics.heightPixels;
        int width1 = displayMetrics.widthPixels;
        //params.setPreviewFormat(format);
        params.setPictureSize(selected.width,selected.height);
        mCamera.setParameters(params);
        mCamera.startPreview();
        //tryDrawing(surfaceHolder);

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("PREVIEW","surfaceDestroyed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        Log.d("CameraPreviewActivity", "ENtering camera preview activity");
        mPreview = (SurfaceView)findViewById(R.id.preview);

        mPreview.getHolder().addCallback(this);
        mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCamera = Camera.open();
        controlInflater = LayoutInflater.from(getBaseContext());
        CustomView customView = (CustomView)findViewById(R.id.customView);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }
    private void tryDrawing(SurfaceHolder holder) {
        Log.i("Camera view", "Trying to draw...");

        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            Log.e("Camera view", "Cannot draw onto the canvas as it's null");
        } else {
            drawMyStuff(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawMyStuff(final Canvas canvas) {
        Random random = new Random();
        Log.i("Camera view", "Drawing...");
        canvas.drawRGB(255, 128, 128);
    }

    public class MyView extends View{
        Paint paint;
        RectF rect;
        public MyView(Context context) {
            super(context);
            rect = new RectF(20, 20, 100,100);
            //canvas.drawOval(new RectF(50, 50, 20, 40), p)
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            setMeasuredDimension(200, 200);

        }



        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawOval(rect, paint);

        }
    }

}
