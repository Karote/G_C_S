package com.coretronic.drone.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.piloting.Setting;

/**
 * Created by jiaLian on 15/3/11.
 */
public class ViewManager {
    private static final String TAG = ViewManager.class.getSimpleName();

    public static void assignSingleSelectionButton(final MainActivity activity, final View view, final Setting.SettingType settingType, int[] ids, int[] tags) {
        int currentValue = activity.getSettingValue(settingType);
        final Button[] btns = new Button[ids.length];

        for (int i = 0; i < ids.length; i++) {
            btns[i] = (Button) view.findViewById(ids[i]);
            btns[i].setTag(tags[i]);

            if (currentValue == tags[i]) {
                btns[i].setSelected(true);
            }

            btns[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = (int) v.getTag();
                    activity.setSettingValue(settingType, value);

                    for (Button btn : btns) {
                        if ((int) btn.getTag() == value) {
                            btn.setSelected(true);
                        } else {
                            btn.setSelected(false);
                        }
                    }

                    Setting setting = activity.getSetting(settingType);

                    if (setting.getParameterType() != null && activity.getDroneController() != null) {
                        activity.getDroneController().setParameters(setting.getParameterType(), setting.getParameter());
                    }

                }
            });
        }

    }

    public static void assignSwitchView(final MainActivity activity, View view, int id, final Setting.SettingType settingType) {
        final Switch sw = (Switch) view.findViewById(id);
        final Setting setting = activity.getSetting(settingType);
        final int type = settingType.ordinal();
        sw.post(new Runnable() {
            @Override
            public void run() {
                sw.setChecked(setting.getValue() == Setting.ON);
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        activity.setSettingValue(settingType, isChecked == true ? Setting.ON : Setting.OFF);
                        Log.d(TAG, "isSetting[" + type + "]: " + setting.getValue());
                        if (setting.getParameterType() != null && activity.getDroneController() != null) {
                            activity.getDroneController().setParameters(setting.getParameterType(), setting.getParameter());
                        }
                    }
                });
            }
        });
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
