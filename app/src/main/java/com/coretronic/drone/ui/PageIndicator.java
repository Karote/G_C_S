package com.coretronic.drone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.coretronic.drone.R;

/**
 * Created by jiaLian on 15/6/15.
 */
public class PageIndicator extends LinearLayout {
    private static final int DEFAULT_PAGE_COUNT = 4;
    private int pageCount = DEFAULT_PAGE_COUNT;
    private ImageView[] indicators;
    private int normalItemResId = R.drawable.icon_page_dot_off;
    private int currentItemResId = R.drawable.icon_page_dot_on;
    private int size;
    private int margin;

    public PageIndicator(Context context) {
        super(context);
        setPageCount(DEFAULT_PAGE_COUNT);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPageCount(DEFAULT_PAGE_COUNT);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        setCurrentItem(true, 0);
    }

    public void setCurrentItem(int position) {
        setCurrentItem(false, position);
    }

    private void setCurrentItem(boolean isNew, int position) {
        for (int i = 0; i < indicators.length; i++) {
            if (isNew) {
                indicators[i]=new ImageView(getContext());
                LayoutParams params = new LayoutParams(size, size);
                if (i == indicators.length - 1) {
                    params.rightMargin = 0;
                } else {
                    params.rightMargin = margin;
                }
                addView(indicators[i], params);
            }
            if (i == position) {
                indicators[i].setBackgroundResource(currentItemResId);
            } else {
                indicators[i].setBackgroundResource(normalItemResId);
            }
        }
    }

    public void setIndicatorSize(int size) {
        this.size = size;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public void setPageCount(int count) {
        indicators = new ImageView[count];
        removeAllViews();
        init(getContext());
    }

}
