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
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.coretronic.drone.R;

/**
 * Created by jiaLian on 15/3/30.
 */
public class JoyStickSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private static final String TAG = JoyStickSurfaceView.class.getSimpleName();

    private static final int ALPHA_MAX = 255;
    private static final float ALPHA_SCALE = 0.57f;

    public static final int CONTROL_TYPE_PITCH_ROLL = 1;
    public static final int CONTROL_TYPE_THROTTLE_YAW = 2;
    public static final int CONTROL_TYPE_PITCH_YAW = 3;
    public static final int CONTROL_TYPE_THROTTLE_ROLL = 4;

    private static final int DISTANCE_TOLERANCE = 20;
    private static final int TIME_DELAY = 60;

    private Bitmap throttleUpBitmap;
    private Bitmap yawRightBitmap;
    private Bitmap arrowUpBitmap;
    private Bitmap arrowRightBitmap;

    private Paint stickPaint = null;

    private SurfaceHolder surfaceHolder;

    private Point startPoint;
    private Point rockerPoint;
    private int padRadius;
    private int stickShiftRadius;

    private boolean isStop;
    private boolean isJoypad;
    private int controlType;

    private int paintPressedAlpha;
    private int paintNormalAlpha;

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
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);

        int indicatorSize = (int) getResources().getDimension(R.dimen.joypad_indicator_size);
        throttleUpBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_speed_up), indicatorSize, indicatorSize);
        yawRightBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_spin_right), indicatorSize, indicatorSize);
        arrowUpBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_arrow_up), indicatorSize, indicatorSize);
        arrowRightBitmap = getResizedBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ico_joypad_arrow_right), indicatorSize, indicatorSize);
    }

    public void setPaintPressedAlpha(float rate) {
        paintPressedAlpha = (int) (rate * ALPHA_MAX);
        paintNormalAlpha = (int) (paintPressedAlpha * ALPHA_SCALE);
        stickPaint.setAlpha(paintNormalAlpha);
    }

    public void initJoyMode(int controlType, boolean isJoypad, int alpha) {
        this.controlType = controlType;
        this.isJoypad = isJoypad;
        setPaintPressedAlpha(alpha / 100f);
    }

    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        setMeasuredDimension(width, height);

        startPoint = new Point(width >> 1, height >> 1);
        rockerPoint = new Point(startPoint);
        padRadius = (int) ((width >> 1) - getResources().getDimension(R.dimen.joypad_rim_width) / 2f);
        stickShiftRadius = (int) (padRadius - getResources().getDimension(R.dimen.stick_size) / 2f);
  }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Thread thread = new Thread(this);
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
            if (stickListener != null && !isJoypad) {
                stickListener.onOrientationAction(MotionEvent.ACTION_DOWN);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (distance <= stickShiftRadius) {
                rockerPoint.set(x, y);
            } else {
                rockerPoint = getBorderPoint(startPoint, new Point(x, y), stickShiftRadius);
            }
            int dx = rockerPoint.x - startPoint.x;
            int dy = rockerPoint.y - startPoint.y;

            if (Math.abs(dx) >= DISTANCE_TOLERANCE || Math.abs(dy) >= DISTANCE_TOLERANCE) {
                if (stickListener != null && isJoypad) {
                    stickListener.onStickMoveEvent(JoyStickSurfaceView.this, action, dx, -dy);
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            rockerPoint.set(startPoint.x, startPoint.y);
            if (stickListener != null && isJoypad) {
                stickListener.onStickMoveEvent(JoyStickSurfaceView.this, action, 0, 0);
            } else if (stickListener != null && !isJoypad) {
                stickListener.onOrientationAction(MotionEvent.ACTION_UP);
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

    public int getStickShiftRadius() {
        return stickShiftRadius;
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
            if (isJoypad) {
                Paint padPaint = new Paint(stickPaint);
                padPaint.setAlpha((int) (stickPaint.getAlpha() * ALPHA_SCALE));
                canvas.drawCircle(startPoint.x, startPoint.y, padRadius, padPaint);
                canvas.drawCircle(rockerPoint.x, rockerPoint.y, circleRadius, stickPaint);

                padPaint.setStrokeWidth(getResources().getDimension(R.dimen.joypad_rim_width));
                padPaint.setStyle(Paint.Style.STROKE);
                padPaint.setAlpha(stickPaint.getAlpha());
                canvas.drawCircle(startPoint.x, startPoint.y, padRadius, padPaint);

                switch (controlType) {
                    case CONTROL_TYPE_THROTTLE_YAW:
                        drawIndicator(canvas, throttleUpBitmap, yawRightBitmap);
                        break;
                    case CONTROL_TYPE_PITCH_ROLL:
                        drawIndicator(canvas, arrowUpBitmap);
                        break;
                    case CONTROL_TYPE_THROTTLE_ROLL:
                        drawIndicator(canvas, throttleUpBitmap, arrowRightBitmap);
                        break;
                    case CONTROL_TYPE_PITCH_YAW:
                        drawIndicator(canvas, arrowUpBitmap, yawRightBitmap);
                        break;
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
        canvas.drawBitmap(src[0], padRadius - halfIndicatorSize, 0, this.stickPaint);
        canvas.drawBitmap(rotateBitmap(src[0], 180), padRadius - halfIndicatorSize, (padRadius << 1) - indicatorSize, this.stickPaint);
        if (src.length == 1) {
            canvas.drawBitmap(rotateBitmap(src[0], 90), (padRadius << 1) - indicatorSize, padRadius - halfIndicatorSize, this.stickPaint);
            canvas.drawBitmap(rotateBitmap(src[0], 270), 0, padRadius - halfIndicatorSize, this.stickPaint);
        } else {
            canvas.drawBitmap(src[1], (padRadius << 1) - indicatorSize, padRadius - halfIndicatorSize, this.stickPaint);
            canvas.drawBitmap(mirrorXBitmap(src[1]), 0, padRadius - halfIndicatorSize, this.stickPaint);

        }
    }


    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
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

    public interface OnStickListener {
        void onStickMoveEvent(View view, int action, int dx, int dy);

        void onOrientationAction(int action);

        void onDoubleClick(View view);
    }
}
