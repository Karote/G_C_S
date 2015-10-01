package com.coretronic.drone.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by karot.chuang on 2015/10/1.
 */
public class TextViewWithoutPaddings extends TextView {

    private final Paint mPaint = new Paint();

    private final Rect mBounds = new Rect();

    public TextViewWithoutPaddings(Context context) {
        super(context);
    }

    public TextViewWithoutPaddings(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewWithoutPaddings(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final String text = calculateTextParams();

        final int left = mBounds.left;
        final int bottom = mBounds.bottom;
        mBounds.offset(-mBounds.left, -mBounds.top);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getCurrentTextColor());
        canvas.drawText(text, -left, mBounds.bottom - bottom, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        calculateTextParams();
        setMeasuredDimension(mBounds.width() + 1, -mBounds.top + 1);
    }

    private String calculateTextParams() {
        final String text = getText().toString();
        mPaint.setTextSize(getTextSize());
        mPaint.setTypeface(getTypeface());
        mPaint.getTextBounds(text, 0, text.length(), mBounds);
        return text;
    }
}
