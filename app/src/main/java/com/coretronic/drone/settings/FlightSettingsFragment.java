package com.coretronic.drone.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.drone.util.ViewManager;
import com.coretronic.drone.util.AppConfig;

import de.greenrobot.event.EventBus;

/**
 * Created by jiaLian on 15/4/1.
 */
public class FlightSettingsFragment extends UnBindDrawablesFragment {
    private static final String TAG = FlightSettingsFragment.class.getSimpleName();
    private MainActivity mMainActivity;
    private SharedPreferences mSharedPreferences;

    private SeekBarTextView mLowPowerLevelOneSeekBarTextView;
    private SeekBarTextView mLowPowerLevelTwoSeekBarTextView;
    private EventBus mEventBus;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
        mSharedPreferences = mMainActivity.getPreferences(Context.MODE_PRIVATE);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_flight_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_rotation_max, Setting.SettingType.ROTATION_SPEED_MAX);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_tilt_angle_max, Setting.SettingType.TILT_ANGLE_MAX);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_vertical_speed_max, Setting.SettingType.VERTICAL_SPEED_MAX);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_altitude_max, Setting.SettingType.ALTITUDE_LIMIT);
        ViewManager.assignSwitchView(mMainActivity, view, R.id.switch_low_power_level_one, Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_ENABLE);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_low_power_flash, Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_VALUE);
        ViewManager.assignSwitchView(mMainActivity, view, R.id.switch_low_power_level_two, Setting.SettingType
                .LOW_BATTERY_PROTECTION_CRITICAL_ENABLE);
        ViewManager.assignSettingSeekBarTextView(mMainActivity, view, R.id.setting_bar_low_power_rtl, Setting.SettingType.LOW_BATTERY_PROTECTION_CRITICAL_VALUE);

        Switch mLowPowerLevelOneSwitch = (Switch) view.findViewById(R.id.switch_low_power_level_one);
        Switch mLowPowerLevelTwoSwitch = (Switch) view.findViewById(R.id.switch_low_power_level_two);

        mLowPowerLevelOneSeekBarTextView = (SeekBarTextView) view.findViewById(R.id.setting_bar_low_power_flash);
        mLowPowerLevelTwoSeekBarTextView = (SeekBarTextView) view.findViewById(R.id.setting_bar_low_power_rtl);

        mLowPowerLevelOneSeekBarTextView.setViewEnabled(mLowPowerLevelOneSwitch.isChecked());
        mLowPowerLevelTwoSeekBarTextView.setViewEnabled(mLowPowerLevelTwoSwitch.isChecked());

        final TextView flatTrimExtTime = (TextView) view.findViewById(R.id.tv_flat_trim_exe_time);
        flatTrimExtTime.setText(mSharedPreferences.getString(AppConfig.PREF_FLAT_TRIM_LAST_TIME, ""));
        final Time time = new Time();
        Button btnFlatTrim = (Button) view.findViewById(R.id.btn_flat_trim);
        btnFlatTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMainActivity.getDroneController() != null) {
                    mMainActivity.getDroneController().flatTrim();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            time.set(System.currentTimeMillis());
                            String str = time.format("%d/%m/%Y");
                            flatTrimExtTime.setText(str);
                            mSharedPreferences.edit()
                                    .putString(AppConfig.PREF_FLAT_TRIM_LAST_TIME, str)
                                    .apply();
                        }
                    });
                } else {
                    Toast.makeText(mMainActivity, "Drone Controller is null!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventBus.unregister(this);
    }

    public void onEventMainThread(SwitchEvent switchEvent) {
        if (switchEvent == null) {
            return;
        }
        switch (switchEvent.getId()) {
            case R.id.switch_low_power_level_one:
                mLowPowerLevelOneSeekBarTextView.setViewEnabled(switchEvent.isChecked());
                break;
            case R.id.switch_low_power_level_two:
                mLowPowerLevelTwoSeekBarTextView.setViewEnabled(switchEvent.isChecked());
                break;
        }
    }

}
