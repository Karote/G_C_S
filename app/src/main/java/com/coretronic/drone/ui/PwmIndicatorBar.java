package com.coretronic.drone.ui;

import android.content.Context;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2016/2/17.
 */
public class PwmIndicatorBar extends PercentRelativeLayout {
    private View mFocusBar;

    public PwmIndicatorBar(Context context) {
        super(context);
        init(context);
    }

    public PwmIndicatorBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PwmIndicatorBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pwm_indicator_bar, this);

        mFocusBar = this.findViewById(R.id.focus_bar);
        setPercentageValue(0);
    }

    public void setPercentageValue(float value){
        PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) mFocusBar.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
        info.leftMarginPercent = value;
        mFocusBar.requestLayout();
    }
}
