package com.coretronic.drone.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.coretronic.drone.DroneG2Application;
import com.coretronic.drone.R;

/**
 * Created by jiaLian on 15/3/30.
 */
public class JoyStickSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    public interface OnStickListener {
        void onStickMoveEvent(View view, int action, int dx, int dy);

        void onOrientationSensorMode(int action);

        void onDoubleClick(View view);
    }

    private static final String TAG = JoyStickSurfaceView.class.getSimpleName();

    public static final int CONTROL_TYPE_PITCH_ROLL = 1;
    public static final int CONTROL_TYPE_THROTTLE_YAW = 2;

    public static final int DISTANCE_TOLERANCE = 20;
    public static final int TIME_DELAY = 100;
    private static final int PAINT_PRESSED_ALPHA_DEFAULT = 180;
    public static final float ALPHA_SCALE = 0.57f;

    private Bitmap throttleUpBitmap;
    private Bitmap yawRightBitmap;
    private Bitmap arrowUpBitmap;

    private Paint stickPaint = null;

    private SurfaceHolder surfaceHolder;

    private Thread thread;

    private Point startPoint;
    private Point rockerPoint;
    private int radius;

    private boolean isStop;
    private boolean isJoypadMode;
    private int controlType;

    private int paintPressedAlpha = 180;
    private int paintNormalAlpha = (int) (PAINT_PRESSED_ALPHA_DEFAULT * ALPHA_SCALE);

    private OnStickListener stickListener;
    private GestureDetector gestureDetector;


    public JoyStickSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public JoyStickSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        setKeepScreenOn(true);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        stickPaint = new Paint();
        stickPaint.setColor(Color.WHITE);
        stickPaint.setAntiAlias(true);
        stickPaint.setAlpha(paintNormalAlpha);

        gestureDetector = new GestureDetector(context, simpleOnGestureListener);

    }

    public void setPaintPressedAlpha(int alpha) {
        paintPressedAlpha = alpha;
        paintNormalAlpha = (int) (paintPressedAlpha * ALPHA_SCALE);
    }

    public void initJoyMode(int controlType, boolean isJoypadMode) {
        this.controlType = controlType;
        this.isJoypadMode = isJoypadMode;
        int indicatorSize = (int) getResources().getDimension(R.dimen.joypad_indicator_size);
        throttleUpBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_speed_up), indicatorSize, indicatorSize);
        yawRightBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_spin_right), indicatorSize, indicatorSize);
        arrowUpBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_arrow_up), indicatorSize, indicatorSize);

        if (stickListener != null) {
            stickListener.onStickMoveEvent(JoyStickSurfaceView.this, MotionEvent.ACTION_UP, 0, 0);
        }
    }

    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);

        startPoint = new Point(width >> 1, height >> 1);
        rockerPoint = new Point(startPoint);
        radius = (int) ((width >> 1) -getResources().getDimension(R.dimen.joypad_rim_width)/2f);
        ((DroneG2Application) this.getContext().getApplicationContext()).joyStickRadius = radius;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new Thread(this);
        isStop = false;
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isStop = true;
    }

    @Override
    public void run() {
        while (!isStop) {
            draw();
            try {
                Thread.sleep(TIME_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        int distance = getDistance(startPoint.x, startPoint.y, x, y);
        if (action == MotionEvent.ACTION_DOWN) {
            stickPaint.setAlpha(paintPressedAlpha);
//            bgPaint.setAlpha(200 / 4);
            if (stickListener != null && !isJoypadMode) {
                stickListener.onOrientationSensorMode(MotionEvent.ACTION_DOWN);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (distance <= radius) {
                rockerPoint.set(x, y);
            } else {
                rockerPoint = getBorderPoint(startPoint, new Point(x, y), radius);
            }
            int dx = rockerPoint.x - startPoint.x;
            int dy = rockerPoint.y - startPoint.y;

            if (Math.abs(dx) >= DISTANCE_TOLERANCE || Math.abs(dy) >= DISTANCE_TOLERANCE) {
                if (stickListener != null && isJoypadMode) {
                    stickListener.onStickMoveEvent(JoyStickSurfaceView.this, action, dx, -dy);
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            rockerPoint.set(startPoint.x, startPoint.y);
            if (stickListener != null && isJoypadMode) {
                stickListener.onStickMoveEvent(JoyStickSurfaceView.this, action, 0, 0);
            } else if (stickListener != null && !isJoypadMode) {
                stickListener.onOrientationSensorMode(MotionEvent.ACTION_UP);
            }
            stickPaint.setAlpha(paintNormalAlpha);
        }
        return gestureDetector.onTouchEvent(event);
    }

    SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            stickListener.onDoubleClick(JoyStickSurfaceView.this);
            return true;
        }
    };

    public int getRadius() {
        return radius;
    }

    public int getControlType() {
        return controlType;
    }

    public void setOnStickListener(OnStickListener stickListener) {
        this.stickListener = stickListener;
    }

    private void draw() {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            float circleRadius = getResources().getDimension(R.dimen.stick_size) / 2;
            if (isJoypadMode) {
                Paint padPaint = new Paint(stickPaint);
                padPaint.setAlpha((int) (stickPaint.getAlpha() * ALPHA_SCALE));
                canvas.drawCircle(startPoint.x, startPoint.y, radius, padPaint);
                canvas.drawCircle(rockerPoint.x, rockerPoint.y, circleRadius, this.stickPaint);

                padPaint.setStrokeWidth(getResources().getDimension(R.dimen.joypad_rim_width));
                padPaint.setStyle(Paint.Style.STROKE);
                padPaint.setAlpha(stickPaint.getAlpha());
                canvas.drawCircle(startPoint.x, startPoint.y, radius, padPaint);

                if (controlType == JoyStickSurfaceView.CONTROL_TYPE_THROTTLE_YAW) {
                    drawIndicator(canvas, throttleUpBitmap, yawRightBitmap);
                } else {
                    drawIndicator(canvas, arrowUpBitmap);
                }
            } else {
                canvas.drawCircle(startPoint.x, startPoint.y, circleRadius, stickPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawIndicator(Canvas canvas, Bitmap... src) {
        int indicatorSize = src[0].getWidth();
        int halfIndicatorSize = indicatorSize >> 1;
        canvas.drawBitmap(src[0], radius - halfIndicatorSize, 0, this.stickPaint);
        canvas.drawBitmap(rotateBitmap(src[0], 180), radius - halfIndicatorSize, (radius << 1) - indicatorSize, this.stickPaint);
        if (src.length == 1) {
            canvas.drawBitmap(rotateBitmap(src[0], 90), (radius << 1) - indicatorSize, radius - halfIndicatorSize, this.stickPaint);
            canvas.drawBitmap(rotateBitmap(src[0], 270), 0, radius - halfIndicatorSize, this.stickPaint);
        } else {
            canvas.drawBitmap(src[1], (radius << 1) - indicatorSize, radius - halfIndicatorSize, this.stickPaint);
            canvas.drawBitmap(mirrorXBitmap(src[1]), 0, radius - halfIndicatorSize, this.stickPaint);

        }
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap mirrorXBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }

    private int getDistance(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private Point getBorderPoint(Point a, Point b, int cutRadius) {
        float radian = getRadian(a, b);
        return new Point(a.x + (int) (cutRadius * Math.cos(radian)), a.y
                + (int) (cutRadius * Math.sin(radian)));
    }

    // tangle
    private float getRadian(Point a, Point b) {
        float lenA = b.x - a.x;
        float lenB = b.y - a.y;
        float lenC = (float) Math.sqrt(lenA * lenA + lenB * lenB);
        float ang = (float) Math.acos(lenA / lenC);
        ang = ang * (b.y < a.y ? -1 : 1);
        return ang;
    }
}
