package com.coretronic.drone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coretronic.drone.R;

import java.text.DecimalFormat;

/**
 * Created by jiaLian on 15/6/15.
 */
public class SeekBarTextView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    private float mUiMinValue;
    private float mUiMaxValue;
    private float mUiGapValue;
    private float mSettingMinValue;
    private float mSettingMaxValue;
    private float mSettingGapValue;
    private int mSeekBarLevel;
    private DecimalFormat mDecimalFormat;

    private TextView tvValue;
    private TextView tvUnit;
    private SeekBar seekBar;

    private SeekBarTextViewChangeListener seekBarTextViewChangeListener;

    public SeekBarTextView(Context context) {
        super(context);
    }

    public SeekBarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_seek_bar, null);
        tvValue = (TextView) view.findViewById(R.id.tv_value);
        tvUnit = (TextView) view.findViewById(R.id.tv_unit);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarTextView, 0, 0);
            tvUnit.setText(a.getString(R.styleable.SeekBarTextView_unit));
            a.recycle();
        }

        addView(view);
    }

    public void registerSeekBarTextViewChangeListener(SeekBarTextViewChangeListener seekBarTextViewChangeListener) {
        this.seekBarTextViewChangeListener = seekBarTextViewChangeListener;
    }

    public void setConfig(float uiMin, float uiMax, float uiGap, float settingMin, float settingMax) {
        this.mUiMinValue = uiMin;
        this.mUiMaxValue = uiMax;
        this.mUiGapValue = uiGap;
        this.mSettingMinValue = settingMin;
        this.mSettingMaxValue = settingMax;

        if (mUiGapValue < 1) {
            mDecimalFormat = new DecimalFormat("#.#");
        } else {
            mDecimalFormat = new DecimalFormat("#");
        }


        mSeekBarLevel = (int) ((mUiMaxValue - mUiMinValue) / mUiGapValue);
        seekBar.setMax(mSeekBarLevel);

        this.mSettingGapValue = (mSettingMaxValue - mSettingMinValue) / mSeekBarLevel;
    }

    private float settingValueToUiValue(float settingValue) {
        return ((settingValue - mSettingMinValue) / mSettingGapValue) * mUiGapValue + mUiMinValue;
    }

    private int settingValueToProgressLevel(float settingValue) {
        return (int) ((settingValue - mSettingMinValue) / mSettingGapValue);
    }

    private float progressLevelToUiValue(int progress) {
        return (progress * mUiGapValue) + mUiMinValue;
    }

    private float progressLevelToSetValue(int progress) {
        return (progress * mSettingGapValue) + mSettingMinValue;
    }

    public void setValue(final float settingValue) {
        tvValue.setText(mDecimalFormat.format(settingValueToUiValue(settingValue)));
        seekBar.setProgress(settingValueToProgressLevel(settingValue));
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(settingValueToProgressLevel(settingValue));
            }
        });
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setViewDisable(float defaultValue) {
        tvValue.setEnabled(false);
        seekBar.setEnabled(false);
        tvValue.setTextColor(getResources().getColor(R.color.gray));
        tvUnit.setTextColor(getResources().getColor(R.color.gray));
        tvValue.setText(mDecimalFormat.format(settingValueToUiValue(defaultValue)));
        seekBar.setProgress(settingValueToProgressLevel(defaultValue));
    }

    @Override
    public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            tvValue.setText(mDecimalFormat.format(progressLevelToUiValue(progress)));
        }
    }

    @Override
    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
        if (seekBarTextViewChangeListener != null) {
            seekBarTextViewChangeListener.onStopTrackingTouch(progressLevelToSetValue(seekBar.getProgress()));
        }
    }

    public interface SeekBarTextViewChangeListener {
        void onStopTrackingTouch(float value);
    }
}
