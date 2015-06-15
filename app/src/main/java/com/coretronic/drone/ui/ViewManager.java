package com.coretronic.drone.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.coretronic.drone.DroneG2Application;
import com.coretronic.drone.R;
import com.coretronic.drone.piloting.Setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jiaLian on 15/3/11.
 */
public class ViewManager {
    private static final String TAG = ViewManager.class.getSimpleName();
    public static final float HALF_ALPHA = 0.3f;
    public static final float NON_ALPHA = 1f;

    public static void assignSwitchView(View view, int id, Setting.SettingType settingType) {
        Switch sw = (Switch) view.findViewById(id);
        final int type = settingType.ordinal();
        sw.setChecked(DroneG2Application.settings[type].getValue() == Setting.ON ? true : false);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    DroneG2Application.settings[type].setValue(isChecked == true ? Setting.ON : Setting.OFF);
                    Log.d(TAG, "isSetting[" + type + "]: " + DroneG2Application.settings[type].getValue());
                }
            }
        });
    }

    public static void assignSettingSeekBarView(View view, int id, Setting.SettingType settingType) {
        SettingSeekBar settingSeekBar = (SettingSeekBar) view.findViewById(id);
        settingSeekBar.setConfig(20, 80, "%");
        settingSeekBar.registerSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
            }
        });
        settingSeekBar.setValue(60);
    }

//    public static void assignSeekBarView(View view, int id, Setting.SettingType settingType) {
//        SeekBar seekBar = (SeekBar) view.findViewById(id);
//        final Setting setting = DroneG2Application.settings[settingType.ordinal()];
//        seekBar.setMax(setting.getMaxValue() - setting.getMinVale());
//        seekBar.setProgress(setting.getValue() - setting.getMinVale());
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                setting.setValue(seekBar.getProgress() + setting.getMinVale());
//            }
//        });
//    }

    public static void setEnabled(boolean enabled, View[] viewArray, View... views) {
        List<View> combineAll = new ArrayList<View>(viewArray.length + views.length);
        Collections.addAll(combineAll, viewArray);
        Collections.addAll(combineAll, views);
        doEnabled(enabled, combineAll.toArray(new View[combineAll.size()]));
    }

    public static void setEnabled(boolean enabled, View... views) {
        doEnabled(enabled, views);
    }

    private static void doEnabled(boolean enabled, View[] views) {
        float alpha = setAlpha(enabled);
        for (View view : views) {
            view.setEnabled(enabled);
            view.setAlpha(alpha);
        }
    }

    private static float setAlpha(boolean enabled) {
        return enabled ? NON_ALPHA : HALF_ALPHA;
    }

    public static void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
