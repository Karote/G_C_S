package com.coretronic.drone.piloting.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.drone.UnBindDrawablesFragment;
import com.coretronic.drone.piloting.Setting;
import com.coretronic.drone.ui.SeekBarTextView;

/**
 * Created by jiaLian on 15/4/1.
 */
public class FlightSettingsFragment extends UnBindDrawablesFragment {
    private static final String TAG = FlightSettingsFragment.class.getSimpleName();
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_settings_flight_page, container, false);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_rotation_max, Setting.SettingType.ROTATION_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_tilt_angle_max, Setting.SettingType.TILT_ANGLE_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_vertical_speed_max, Setting.SettingType.VERTICAL_SPEED_MAX);
        SeekBarTextView.assignSettingSeekBarTextView(activity, fragmentView, R.id.setting_bar_altitude_max, Setting.SettingType.ALTITUDE_LIMIT);

        Button btnFlatTrim = (Button) fragmentView.findViewById(R.id.btn_flat_trim);
        btnFlatTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.getDroneController() != null) {
                    activity.getDroneController().flatTrim();
                }
            }
        });
        return fragmentView;
    }
}
