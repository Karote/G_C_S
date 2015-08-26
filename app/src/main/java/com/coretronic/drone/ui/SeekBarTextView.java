package com.coretronic.drone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coretronic.drone.MainActivity;
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

    public static void assignSettingSeekBarTextView(final MainActivity activity, final View view, int id, final Setting.SettingType settingType) {
        final Setting setting = activity.getSetting(settingType);
        Log.d(TAG, "setting: " + setting.getMinValue() + ", " + setting.getMaxValue() + ", " + setting.getValue() + ", " + setting.getUnit());
        SeekBarTextView seekBarTextView = (SeekBarTextView) view.findViewById(id);
        seekBarTextView.setConfig(setting.getMinValue(), setting.getMaxValue(), setting.getUnit());
        seekBarTextView.setValue(setting.getValue());
        seekBarTextView.registerSeekBarTextViewChangeListener(new SeekBarTextView.SeekBarTextViewChangeListener() {

            @Override
            public void onStopTrackingTouch(int value) {
                Log.d(TAG, "onStopTrackingTouch");
                activity.setSettingValue(settingType, value);
                if (setting.getParameterType() != null && activity.getDroneController() != null) {
                    activity.getDroneController().setParameters(setting.getParameterType(), setting.getParameter());
                }
            }
        });
        switch (id) {
            case R.id.setting_bar_low_power_flash:
            case R.id.setting_bar_low_power_rtl:
                seekBarTextView.setTvWidth(28);
                break;
        }
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_seek_bar, null);
        tvValue = (TextView) view.findViewById(R.id.tv_value);
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
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

    public void setValue(final int value) {
        tvValue.setText(String.valueOf(value) + unit);
        seekBar.setProgress(value - minValue);
        seekBar.post(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(value - minValue);
            }
        });
        seekBar.setOnSeekBarChangeListener(this);
    }

    public void setTvWidth(int width) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        tvValue.setLayoutParams(params);
    }

    public void setViewEnabled(boolean flag) {
        tvValue.setEnabled(flag);
        seekBar.setEnabled(flag);
        if (flag) {
            tvValue.setTextColor(getResources().getColor(R.color.blue_sky));
        } else {
            tvValue.setTextColor(getResources().getColor(R.color.seekbar_off_tv_color));
        }
    }

    @Override
    public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            Log.d(TAG, "onProgressChanged " + (progress + minValue));
            tvValue.setText((progress + minValue) + unit);
        }
    }

    @Override
    public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
        if (seekBarTextViewChangeListener != null) {
            seekBarTextViewChangeListener.onStopTrackingTouch(seekBar.getProgress() + minValue);
        }
    }

    public interface SeekBarTextViewChangeListener {
        void onStopTrackingTouch(int value);
    }
}
