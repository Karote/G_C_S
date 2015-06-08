package com.coretronic.drone.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jiaLian on 15/4/9.
 */
public class SemiCircleProgressBarView extends View {
    private int mProgress;
    private int maxProgress=100;
    private RectF mOval;
    private RectF mOvalInner;
    private Paint mPaintProgress;
    private Paint mPaintBase;
    private Paint mPaintClip;
    private float ovalsDiff;
    private Path clipPath;


    public SemiCircleProgressBarView(Context context) {
        super(context);
        init();
    }

    public SemiCircleProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SemiCircleProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mProgress = 0;
        ovalsDiff = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mOval = new RectF();
        mOvalInner = new RectF();
        clipPath = new Path();

        mPaintBase = new Paint();
//        mPaintBase.setStyle(Paint.Style.STROKE);
//        mPaintBase.setStrokeWidth(10);
        mPaintBase.setColor(Color.GRAY);
        mPaintBase.setAntiAlias(true);

        mPaintProgress = new Paint();
//        mPaintProgress.setStyle(Paint.Style.STROKE);
//        mPaintProgress.setStrokeWidth(10);
        mPaintProgress.setColor(Color.GREEN);
        mPaintProgress.setAntiAlias(true);

        mPaintClip = new Paint();
//        mPaintClip.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaintClip.setColor(Color.TRANSPARENT);
        mPaintClip.setAlpha(0);
        mPaintClip.setAntiAlias(true);


    }


    // call this from the code to change the progress displayed
    public void setProgress(int progress) {
        this.mProgress = progress;
        invalidate();
    }

    // sets the width of the progress arc
    public void setProgressBarWidth(float width) {
        this.ovalsDiff = width;
        invalidate();
    }

    // sets the color of the bar (#FF00FF00 - Green by default)
    public void setProgressBarColor(int color) {
        this.mPaintProgress.setColor(color);
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        mOval.set(0, 0, this.getWidth(), this.getHeight() * 2);
        mOvalInner.set(0 + ovalsDiff, 0 + ovalsDiff, this.getWidth() - ovalsDiff, this.getHeight() * 2);
        clipPath.addArc(mOvalInner, 180, 180);
        c.clipPath(clipPath, Region.Op.DIFFERENCE);
        c.drawArc(mOval, 180, 180f, true, mPaintBase);
        c.drawArc(mOval, 180, 180f * ((float) mProgress / maxProgress), true, mPaintProgress);
//        c.drawArc(mOvalInner, 180, 180f, true, mPaintClip);
    }

    // Setting the view to be always a rectangle with height equal to half of its width
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth / 2, parentHeight);
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = parentWidth;
        params.height = parentWidth / 2;
        this.setLayoutParams(params);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
