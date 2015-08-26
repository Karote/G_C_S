package com.coretronic.drone.piloting.settings;

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
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.piloting.settings.module.SwitchEvent;
import com.coretronic.drone.ui.SeekBarTextView;
import com.coretronic.drone.ui.ViewManager;
import com.coretronic.drone.utility.AppConfig;

import de.greenrobot.event.EventBus;

/**
 * Created by jiaLian on 15/4/1.
 */
public class FlightSettingsFragment extends UnBindDrawablesFragment {
    private static final String TAG = FlightSettingsFragment.class.getSimpleName();
    private MainActivity activity;
    private SharedPreferences sharedPreferences;

    private SeekBarTextView flashSeekBarTextView;
    private SeekBarTextView rtlSeekBarTextView;
    private EventBus eventBus;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        sharedPreferences = getActivity().getSharedPreferences(AppConfig.SHAREDPREFERENCE_ID, 0);
        eventBus = EventBus.getDefault();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_flight_page, container, false);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_rotation_max, Setting.SettingType.ROTATION_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_tilt_angle_max, Setting.SettingType.TILT_ANGLE_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_vertical_speed_max, Setting.SettingType.VERTICAL_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_altitude_max, Setting.SettingType.ALTITUDE_LIMIT);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_low_power_level_1, Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_ENABLE);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_low_power_flash, Setting.SettingType.LOW_BATTERY_PROTECTION_WARN_VALUE);
        ViewManager.assignSwitchView(activity, fragmentView, R.id.switch_low_power_level_2, Setting.SettingType.LOW_BATTERY_PROTECTION_CRITICAL_ENABLE);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_low_power_rtl, Setting.SettingType.LOW_BATTERY_PROTECTION_CRITICAL_VALUE);

        Switch mSwitch = (Switch) fragmentView.findViewById(R.id.switch_low_power_level_1);
        Switch mSwitch2 = (Switch) fragmentView.findViewById(R.id.switch_low_power_level_2);

        flashSeekBarTextView = (SeekBarTextView) fragmentView.findViewById(R.id.setting_bar_low_power_flash);
        rtlSeekBarTextView = (SeekBarTextView) fragmentView.findViewById(R.id.setting_bar_low_power_rtl);

        flashSeekBarTextView.setViewEnabled(mSwitch.isChecked());
        rtlSeekBarTextView.setViewEnabled(mSwitch2.isChecked());

        final TextView flatTrimExtTime = (TextView) fragmentView.findViewById(R.id.tv_flat_trim_exe_time);
        flatTrimExtTime.setText(sharedPreferences.getString(AppConfig.PREF_FLAT_TRIM_LAST_TIME, ""));
        final Time time = new Time();
        Button btnFlatTrim = (Button) fragmentView.findViewById(R.id.btn_flat_trim);
        btnFlatTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.getDroneController() != null) {
                    activity.getDroneController().flatTrim();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            time.set(System.currentTimeMillis());
                            String str = time.format("%d/%m/%Y");
                            flatTrimExtTime.setText(str);
                            sharedPreferences.edit()
                                    .putString(AppConfig.PREF_FLAT_TRIM_LAST_TIME, str)
                                    .apply();
                        }
                    });
                } else {
                    Toast.makeText(activity, "Drone Controller is null!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    public void onEventMainThread(SwitchEvent switchEvent) {
        if (switchEvent == null) {
            return;
        }
        switch (switchEvent.getId()) {
            case R.id.switch_low_power_level_1:
                flashSeekBarTextView.setViewEnabled(switchEvent.isChecked());
                break;
            case R.id.switch_low_power_level_2:
                rtlSeekBarTextView.setViewEnabled(switchEvent.isChecked());
                break;
        }
    }

}
