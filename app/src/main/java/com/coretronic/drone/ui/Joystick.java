package com.coretronic.drone.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2016/3/24.
 */
public class Joystick extends FrameLayout {
    private static final String LOG_TAG = Joystick.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;
    private static final int STICK_SETTLE_DURATION_MS = 100;
    private static final Interpolator STICK_SETTLE_INTERPOLATOR = new DecelerateInterpolator();

    private float centerX;
    private float centerY;
    private float radius;

    private View draggedChild;
    private boolean detectingDrag;
    private boolean dragInProgress;

    private float downX, downY;
    private float startDragX, startDragY;
    private int activePointerId = INVALID_POINTER_ID;

    private JoystickListener listener;

    public interface JoystickListener {
        void onDown();

        /**
         * @param degrees -180 -> 180.
         * @param offset  normalized, 0 -> 1.
         */
        void onDrag(float degrees, float offset);

        void onUp();
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;

        if (!hasStick()) {
            Log.w(LOG_TAG, LOG_TAG + " has no draggable stick, and is therefore not functional. " +
                    "Consider adding a child view to act as the stick.");
        }
    }

    public Joystick(Context context) {
        super(context);
    }

    public Joystick(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Joystick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        centerX = (float) w / 2;
        centerY = (float) h / 2;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            recalculateRadius(right - left, bottom - top);
        }
    }

    private void recalculateRadius(int width, int height) {
        float stickHalfWidth = 0;
        float stickHalfHeight = 0;
        if (hasStick()) {
            final View stick = getChildAt(0);
            stickHalfWidth = (float) stick.getWidth() / 2;
            stickHalfHeight = (float) stick.getHeight() / 2;
        }

        radius = (float) Math.min(width, height) / 2 - Math.max(stickHalfWidth, stickHalfHeight);
    }

    private boolean hasStick() {
        return getChildCount() > 0;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (detectingDrag || !hasStick()) {
                    return false;
                }

                downX = event.getX(0);
                downY = event.getY(0);
                activePointerId = event.getPointerId(0);
                if (isTouchOnControlButton(downX, downY)) {
                    onStartDetectingDrag();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (INVALID_POINTER_ID == activePointerId) break;
                if (detectingDrag && dragInProgress) {
                    return true;
                } else if (detectingDrag) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    float latestX = event.getX(pointerIndex);
                    float latestY = event.getY(pointerIndex);

                    float deltaX = latestX - downX;
                    float deltaY = latestY - downY;
                    if (deltaX == 0 && deltaY == 0) {
                        return false;
                    }
                    startDragX = latestX;
                    startDragY = latestY;
                    onDragStart();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId != activePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnded();

                onStopDetectingDrag();
                break;
            }
        }

        return false;
    }

    private boolean isTouchOnControlButton(float touchX, float touchY) {
        double stickHalfWidth;
        double stickHalfHeight;

        if (!hasStick()) {
            return false;
        }
        stickHalfWidth = (double) getChildAt(0).getWidth() / 2;
        stickHalfHeight = (double) getChildAt(0).getHeight() / 2;

        double stickRadius = Math.max(stickHalfWidth, stickHalfHeight);

        return (Math.abs(touchX - centerX) <= stickRadius && Math.abs(touchY - centerY) <= stickRadius);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (!detectingDrag) {
                    return false;
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (INVALID_POINTER_ID == activePointerId) break;

                if (dragInProgress) {
                    int pointerIndex = event.findPointerIndex(activePointerId);
                    float latestX = event.getX(pointerIndex);
                    float latestY = event.getY(pointerIndex);

                    float deltaX = latestX - startDragX;
                    float deltaY = latestY - startDragY;
                    onDrag(deltaX, deltaY);
                    return true;
                } else if (detectingDrag) {
                    onDragStart();
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = event.getActionIndex();
                final int pointerId = event.getPointerId(pointerIndex);

                if (pointerId != activePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnded();

                if (dragInProgress) {
                    onDragStop();
                } else {
                    onStopDetectingDrag();
                }
                return true;
            }
        }

        return false;
    }

    private void onDragStart() {
        dragInProgress = true;
        draggedChild.animate().cancel();
    }

    private void onDragStop() {
        dragInProgress = false;

        draggedChild.animate()
                .translationX(0).translationY(0)
                .setDuration(STICK_SETTLE_DURATION_MS)
                .setInterpolator(STICK_SETTLE_INTERPOLATOR)
                .start();

        onStopDetectingDrag();
    }

    private void onDrag(float dx, float dy) {
        float x = dx;
        float y = dy;

        float offset = (float) Math.sqrt(x * x + y * y);
        if (x * x + y * y > radius * radius) {
            x = radius * x / offset;
            y = radius * y / offset;
            offset = radius;
        }

        final double radians = Math.atan2(-y, x);
        final float degrees = (float) (180 * radians / Math.PI);

        if (null != listener) {
            listener.onDrag(degrees, 0 == radius ? 0 : offset / radius);
        }

        draggedChild.setTranslationX(x);
        draggedChild.setTranslationY(y);
    }

    private void onStartDetectingDrag() {
        detectingDrag = true;
        draggedChild = getChildAt(0);
        ((ImageButton) draggedChild).setImageResource(R.drawable.btn_gimble_controller_p);
        if (null != listener) {
            listener.onDown();
        }
    }

    private void onStopDetectingDrag() {
        detectingDrag = false;
        if (null != listener) {
            listener.onUp();
        }
        ((ImageButton) draggedChild).setImageResource(R.drawable.btn_gimble_controller_n);
        draggedChild = null;
    }

    private void onTouchEnded() {
        activePointerId = INVALID_POINTER_ID;
    }

    /*
    FORCE SQUARE
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        LayoutParams params = new LayoutParams(getContext(), attrs);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        LayoutParams params = new LayoutParams(p);
        params.gravity = Gravity.CENTER;
        return params;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(LOG_TAG + " can host only one direct child");
        }

        super.addView(child, index, params);
    }
}