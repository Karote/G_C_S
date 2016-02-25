package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.coretronic.drone.R;
import com.coretronic.ibs.log.Logger;

/**
 * Created by karot.chuang on 2016/2/17.
 */
public class RcCalibrationBar extends PercentRelativeLayout implements ToggleButton.OnCheckedChangeListener {
    private String mLabel;

    public RcCalibrationBar(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RcCalibrationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RcCalibrationBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }


    private void init(Context context, AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.rc_calibration_bar, this);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RcCalibrationBar, defStyle, 0);

            mLabel = a.getString(R.styleable.RcCalibrationBar_label);
            ((TextView) this.findViewById(R.id.rc_cali_label)).setText(mLabel);

            a.recycle();
        }

        this.findViewById(R.id.rc_cali_seekbar).setEnabled(false);
        ((ToggleButton) this.findViewById(R.id.reverse_button)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Logger.d("*****Settings - Calibration " + mLabel + ": ON");
        } else {
            Logger.d("*****Settings - Calibration " + mLabel + ": OFF");
        }
    }
}
