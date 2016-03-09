package com.coretronic.drone.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.coretronic.drone.MainActivity;
import com.coretronic.drone.R;
import com.coretronic.ibs.log.Logger;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class GeneralSettingFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private MainActivity mMainActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_general, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View v) {
        ((RadioGroup) v.findViewById(R.id.settings_heading_direction_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_rtl_heading_direction_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.setting_optical_flow_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_show_flight_route_radio_group)).setOnCheckedChangeListener(this);

        v.findViewById(R.id.settings_magnetic_field_deviation_auto_adjust_button).setOnClickListener(this);
        v.findViewById(R.id.settings_flat_trim_execute_button).setOnClickListener(this);
        v.findViewById(R.id.settings_reset_to_default_reset_button).setOnClickListener(this);
        v.findViewById(R.id.settings_firmware_update_check_button).setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){

            case R.id.heading_direction_never_change_button:
                Logger.d("*****Settings - Heading Direction: Never Change");
                break;
            case R.id.heading_direction_next_waypoint_button:
                Logger.d("*****Settings - Heading Direction: Next Waypoint");
                break;
            case R.id.rtl_heading_direction_front_button:
                Logger.d("*****Settings - RTL Heading Direction: Front");
                break;
            case R.id.rtl_heading_direction_rear_button:
                Logger.d("*****Settings - RTL Heading Direction: Rear");
                break;
            case R.id.optical_flow_on_button:
                Logger.d("*****Settings - Optical Flow: ON");
                break;
            case R.id.optical_flow_off_button:
                Logger.d("*****Settings - Optical Flow: OFF");
                break;
            case R.id.show_flight_route_on_button:
                Logger.d("*****Settings - Show Flight Route: ON");
                break;
            case R.id.show_flight_route_off_button:
                Logger.d("*****Settings - Show Flight Route: OFF");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_magnetic_field_deviation_auto_adjust_button:
                Logger.d("*****Settings - Magnetic Field Deviation : Auto adjust");
                break;
            case R.id.settings_flat_trim_execute_button:
                Logger.d("*****Settings - Flat Trim : Execute");
                break;
            case R.id.settings_reset_to_default_reset_button:
                Logger.d("*****Settings - Reset to Default : Reset");
                break;
            case R.id.settings_firmware_update_check_button:
                Logger.d("*****Settings - Firmware Update : Check Now");
                break;
        }
    }
}
