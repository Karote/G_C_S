package com.coretronic.drone.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.coretronic.drone.R;
import com.coretronic.ibs.log.Logger;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class BatteryAndFailsafeSettingFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_battery_failsafe, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View v) {
        ((RadioGroup) v.findViewById(R.id.settings_battery_capacity_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_failsafe_battery_remaining_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_rc_signal_lost_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_gps_signal_lost_radio_group)).setOnCheckedChangeListener(this);
        ((RadioGroup) v.findViewById(R.id.settings_gcs_signal_lost_radio_group)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.battery_capacity_one_thousand_mah:
                Logger.d("*****Settings - BatteryCapacity: 1000 mAh");
                break;
            case R.id.battery_capacity_five_thousand_mah:
                Logger.d("*****Settings - BatteryCapacity: 5000 mAh");
                break;
            case R.id.battery_capacity_ten_thousand_mah:
                Logger.d("*****Settings - BatteryCapacity: 10000 mAh");
                break;
            case R.id.battery_remaining_none_button:
                Logger.d("*****Settings - BatteryRemaining: None");
                break;
            case R.id.battery_remaining_rtl_button:
                Logger.d("*****Settings - BatteryRemaining: RTL");
                break;
            case R.id.battery_remaining_land_button:
                Logger.d("*****Settings - BatteryRemaining: Land");
                break;
            case R.id.rc_signal_lost_none_button:
                Logger.d("*****Settings - RC Signal Lost: None");
                break;
            case R.id.rc_signal_lost_rtl_button:
                Logger.d("*****Settings - RC Signal Lost: RTL");
                break;
            case R.id.rc_signal_lost_land_button:
                Logger.d("*****Settings - RC Signal Lost: Land");
                break;
            case R.id.gps_signal_lost_none_button:
                Logger.d("*****Settings - GPS Signal Lost: None");
                break;
            case R.id.gps_signal_lost_land_button:
                Logger.d("*****Settings - GPS Signal Lost: Land");
                break;
            case R.id.gcs_signal_lost_none_button:
                Logger.d("*****Settings - GCS Signal Lost: None");
                break;
            case R.id.gcs_signal_lost_rtl_button:
                Logger.d("*****Settings - GCS Signal Lost: RTL");
                break;
            case R.id.gcs_signal_lost_land_button:
                Logger.d("*****Settings - GCS Signal Lost: Land");
                break;
        }
    }
}
