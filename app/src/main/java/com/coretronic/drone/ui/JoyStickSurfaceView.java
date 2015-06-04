package com.coretronic.drone.ui;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.coretronic.drone.R;
import com.coretronic.drone.DroneG2Application;

import java.io.InputStream;

/**
 * Created by jiaLian on 15/3/30.
 */
public class JoyStickSurfaceView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    private static final String TAG = JoyStickSurfaceView.class.getSimpleName();

    public static final int CONTROL_TYPE_PITCH_ROLL = 1;
    public static final int CONTROL_TYPE_THROTTLE_YAW = 2;

    public static final int DISTANCE_TOLERANCE = 20;
    public static final int TIME_DELAY = 50;
    private Paint paint = null;
    private SurfaceHolder surfaceHolder;
    private Bitmap stickBitmap;
    private Bitmap bgBitmap;
    private Bitmap resizeStickBitmap = null;
    private Bitmap reSizeBgBitmap = null;
    private Point startPoint;
    private Point rockerPoint;
    private int radius;
    private Thread thread;
    private boolean isStop;
    private OnStickListener stickListener;
    private GestureDetector gestureDetector;
    private boolean isJoypadMode;
    private int controlType;
    private float stickSize = 30;
    private Paint bgPaint;

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
        InputStream inputStream = getResources().openRawResource(R.drawable.redpoint);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(inputStream);
        resizeStickBitmap = bitmapDrawable.getBitmap();
//        stickBitmap = BitmapFactory.decodeResource(context.getResources(), stickDrawableId);
//        bgBitmap = BitmapFactory.decodeResource(getResources(), bgDrawableId);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setAlpha(50);

        if (((DroneG2Application) context.getApplicationContext()).isUITesting) {
            bgPaint = new Paint();
            bgPaint.setColor(Color.RED);
            bgPaint.setAntiAlias(true);
            bgPaint.setAlpha(50);
        }
//        setAlpha(0.3f);

        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
//        gestureDetector.setOnDoubleTapListener(this);

    }

    public void setPaintAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    public void initJoyMode(int controlType, boolean isJoypadMode, int bgDrawableId, int stickDrawableId) {
        this.controlType = controlType;
        this.isJoypadMode = isJoypadMode;
        reSizeBgBitmap = null;
        resizeStickBitmap = null;
        stickBitmap = BitmapFactory.decodeResource(getResources(), stickDrawableId);
        if (bgDrawableId != 0) {
            bgBitmap = BitmapFactory.decodeResource(getResources(), bgDrawableId);
        } else {
            bgBitmap = null;
        }
        if (stickListener != null) {
            stickListener.onStickMoveEvent(JoyStickSurfaceView.this, MotionEvent.ACTION_UP, 0, 0);
        }
    }

    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (stickBitmap != null && resizeStickBitmap == null) {
            if (bgBitmap != null) {
                reSizeBgBitmap = getResizedBitmap(bgBitmap, width, width);
                bgBitmap.recycle();
            }
            resizeStickBitmap = getResizedBitmap(stickBitmap, width / 6, width / 6);

            stickBitmap.recycle();
        }
        setMeasuredDimension(width, height);

        startPoint = new Point(width >> 1, height >> 1);
        rockerPoint = new Point(startPoint);

        if (((DroneG2Application) getContext().getApplicationContext()).isUITesting) {
            radius = width >> 1;
        } else {
            radius = (width - resizeStickBitmap.getWidth()) >> 1;
        }
        ((DroneG2Application) this.getContext().getApplicationContext()).joyStickRadius = radius - 1;
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
            paint.setAlpha(200);
            if (((DroneG2Application) getContext().getApplicationContext()).isUITesting)
                bgPaint.setAlpha(200);
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
            paint.setAlpha(50);
            if (((DroneG2Application) getContext().getApplicationContext()).isUITesting)
                bgPaint.setAlpha(50);
        }
        return gestureDetector.onTouchEvent(event);
    }

    SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            Log.d(TAG,"onDoubleTap");
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

    public void changeStickSize(float stickSize) {
        this.stickSize = stickSize;
    }

    private void draw() {
        if (resizeStickBitmap == null) return;
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            float circleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stickSize / 2, getResources().getDisplayMetrics());
            if (isJoypadMode) {
                if (((DroneG2Application) getContext().getApplicationContext()).isUITesting) {
                    canvas.drawCircle(startPoint.x, startPoint.y, radius, bgPaint);
                    canvas.drawCircle(rockerPoint.x, rockerPoint.y, circleRadius, paint);
                } else {
                    canvas.drawBitmap(reSizeBgBitmap, startPoint.x - (reSizeBgBitmap.getWidth() >> 1), startPoint.y - (reSizeBgBitmap.getHeight() >> 1), paint);
                    canvas.drawBitmap(resizeStickBitmap, rockerPoint.x - (resizeStickBitmap.getWidth() >> 1), rockerPoint.y - (resizeStickBitmap.getHeight() >> 1), paint);
                }

            } else {
                if (((DroneG2Application) getContext().getApplicationContext()).isUITesting) {
                    canvas.drawCircle(startPoint.x, startPoint.y, circleRadius, paint);
                } else {
                    canvas.drawCircle(startPoint.x, startPoint.y, radius / 2, paint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }


    public static interface OnStickListener {
        void onStickMoveEvent(View view, int action, int dx, int dy);

        void onOrientationSensorMode(int action);

        void onDoubleClick(View view);
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
