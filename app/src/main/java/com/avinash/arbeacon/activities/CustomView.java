package com.avinash.arbeacon.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by avinashiyer on 11/5/17.
 */

public class CustomView extends SurfaceView {

    private Paint paint;
    private SurfaceHolder mHolder;
    private Context context;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        this.context = context.getApplicationContext();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        this.context = context.getApplicationContext();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

//    public CustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    public CustomView(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        this.context = context.getApplicationContext();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (mHolder.getSurface().isValid()) {
            canvas = mHolder.lockCanvas();
            Log.d("touch", "touchRecieved by camera");
            if (canvas != null) {
                Log.d("touch", "touchRecieved CANVAS STILL Not Null");

                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(100, 100, 100, 150, paint);
                canvas.drawARGB(100,255,255,255);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                float textSize = paint.getTextSize();
                paint.setTextSize(textSize * 4);
                canvas.drawText("You are near the Malcolm Randall Center", 0,150 , paint);
                paint.setTextSize(textSize);
                //setTextSizeForWidth(pnt,100,"This is a text");
                mHolder.unlockCanvasAndPost(canvas);

            }
        }
    }
    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            invalidate();
//            if (mHolder.getSurface().isValid()) {
//                final Canvas canvas = mHolder.lockCanvas();
//                Log.d("touch", "touchRecieved by camera");
//                if (canvas != null) {
//                    Log.d("touch", "touchRecieved CANVAS STILL Not Null");
//                    canvas.drawColor(Color.RED, PorterDuff.Mode.CLEAR);
//                    canvas.drawColor(Color.RED);
//                    canvas.drawCircle(event.getX(), event.getY(), 2, paint);
//                    mHolder.unlockCanvasAndPost(canvas);
////                    new Handler().postDelayed(new Runnable() {
////                        @Override
////                        public void run() {
////                            Canvas canvas1 = mHolder.lockCanvas();
////                            if(canvas1 !=null){
////                                canvas1.drawColor(0, PorterDuff.Mode.CLEAR);
////                                mHolder.unlockCanvasAndPost(canvas1);
////                            }
////
////                        }
////                    }, 1000);
//
//                }
//                //mHolder.unlockCanvasAndPost(canvas);
//
//
//            }
//        }
//        return false;
//    }
}