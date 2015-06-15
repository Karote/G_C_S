package com.coretronic.drone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coretronic.drone.R;

/**
 * Created by jiaLian on 15/6/15.
 */
public class SettingSeekBar extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    //    private int maxValue;
    private int minValue;
    private String unit;

    private TextView tvValue;
    private SeekBar seekBar;

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener;

    public SettingSeekBar(Context context) {
        super(context);
    }

    public SettingSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_seek_bar, null);
        tvValue = (TextView) view.findViewById(R.id.tv_value);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        addView(view);
    }

    public void registerSeekBarChangeListener(SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        this.seekBarChangeListener = seekBarChangeListener;
    }

    public void setConfig(int minValue, int maxValue, String unit) {
        this.unit = unit;
        this.minValue = minValue;
//        this.maxValue = maxValue;
        seekBar.setMax(maxValue - minValue);
    }

    public void setValue(int value) {
        seekBar.setProgress(value - minValue);
    }

//    public void setUnit(String unit) {
//        this.unit = unit;
//    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvValue.setText((seekBar.getProgress() + minValue) + unit);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBarChangeListener != null) {
            seekBarChangeListener.onStopTrackingTouch(seekBar);
        }
    }
}