package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.coretronic.drone.R;

/**
 * Created by karot.chuang on 2016/2/17.
 */
public class RcCalibrationBar extends PercentRelativeLayout {
    private SeekBar mCaliSeekBar;
    private ToggleButton mReverseButton;
    private TextView mPWMValueTextView;

    private float mPwmMin;
    private float mPwmMax;
    private boolean mCheckRev;
    private onReverseButtonCheckedListener mOnReverseButtonCheckedListener;

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

            String label = a.getString(R.styleable.RcCalibrationBar_label);
            ((TextView) this.findViewById(R.id.rc_cali_label)).setText(label);

            a.recycle();
        }

        mCaliSeekBar = (SeekBar) this.findViewById(R.id.rc_cali_seekbar);
        mCaliSeekBar.setMax(200);
        mCaliSeekBar.setEnabled(false);
        mReverseButton = (ToggleButton) this.findViewById(R.id.reverse_button);
        mReverseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckRev = !mCheckRev;
                mOnReverseButtonCheckedListener.onReverseButtonCheckedChanged(mCheckRev);
            }
        });
        mPWMValueTextView = (TextView) this.findViewById(R.id.rc_calibration_bar_pwm);
    }

    public void setCalibrationConfig(float pwmMin, float pwmMax, boolean checkRev) {
        mPwmMin = pwmMin;
        mPwmMax = pwmMax;
        mCheckRev = checkRev;

        mReverseButton.setChecked(mCheckRev);
    }

    public void setPWMValue(float value) {
        int seekLevel = (int) (((value - mPwmMin) / (mPwmMax - mPwmMin)) * 200);
        mCaliSeekBar.setProgress(seekLevel);
        mPWMValueTextView.setText(String.format("%d%%", seekLevel - 100));
    }

    public interface onReverseButtonCheckedListener {
        void onReverseButtonCheckedChanged(boolean isChecked);
    }

    public void registerReverseButtonCheckedListener(onReverseButtonCheckedListener listener) {
        this.mOnReverseButtonCheckedListener = listener;
    }

    public void setViewEnable(boolean isEnable) {
        this.findViewById(R.id.reverse_button).setEnabled(isEnable);
    }
}
