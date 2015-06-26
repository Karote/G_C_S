package com.coretronic.drone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coretronic.drone.DroneApplication;
import com.coretronic.drone.R;
import com.coretronic.drone.piloting.Setting;

/**
 * Created by jiaLian on 15/6/15.
 */
public class SeekBarTextView extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SeekBarTextView.class.getSimpleName();
    private int minValue;
    private String unit = "";

    private TextView tvValue;
    private SeekBar seekBar;

    private SeekBarTextViewChangeListener seekBarTextViewChangeListener;

    public SeekBarTextView(Context context) {
        super(context);
    }

    public SeekBarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public static void assignSettingSeekBarTextView(final View view, int id, final Setting.SettingType settingType) {
        final Setting setting = DroneApplication.settings[settingType.ordinal()];
        Log.d(TAG, "setting: " + setting.getMinValue() + ", " + setting.getMaxValue() + ", " + setting.getValue() + ", " + setting.getUnit());
        SeekBarTextView seekBarTextView = (SeekBarTextView) view.findViewById(id);
        seekBarTextView.setConfig(setting.getMinValue(), setting.getMaxValue(), setting.getUnit());
        seekBarTextView.setValue(setting.getValue());
        seekBarTextView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {

            @Override
            public void onStopTrackingTouch(int value) {
                Log.d(TAG, "onStopTrackingTouch");
                DroneApplication.settings[settingType.ordinal()].setValue(value);
            }
        });

    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_seek_bar, null);
        tvValue = (TextView) view.findViewById(R.id.tv_value);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        addView(view);
    }

    public void registerSeekBarTextViewChangeListener(SeekBarTextViewChangeListener seekBarTextViewChangeListener) {
        this.seekBarTextViewChangeListener = seekBarTextViewChangeListener;
    }

    public void setConfig(int minValue, int maxValue, String unit) {
        this.unit = unit;
        this.minValue = minValue;
        seekBar.setMax(maxValue - minValue);
    }

    public void setValue(int value) {
        seekBar.setProgress(value - minValue);
        tvValue.setText(value + unit);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvValue.setText((seekBar.getProgress() + minValue) + unit);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBarTextViewChangeListener != null) {
            seekBarTextViewChangeListener.onStopTrackingTouch(seekBar.getProgress() + minValue);
        }
    }

    public interface SeekBarTextViewChangeListener {
        void onStopTrackingTouch(int value);
    }
}