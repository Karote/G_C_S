package com.coretronic.drone.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.coretronic.drone.R;
import com.coretronic.drone.ui.FlightModeModel;
import com.coretronic.ibs.log.Logger;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class RcTxRxSettingFragment extends Fragment {
    private Dialog mFlightModeTypePopupDialog;
    private FlightModeModel mFlightModeModel1;
    private FlightModeModel mFlightModeModel2;
    private FlightModeModel mFlightModeModel3;
    private FlightModeModel mFlightModeModel4;
    private FlightModeModel mFlightModeModel5;
    private FlightModeModel mFlightModeModel6;

    private String mFlightModeTypeSettingValue;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_rc_txrx, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View v) {
        ((RadioGroup) v.findViewById(R.id.settings_receiver_type_radio_group)).setOnCheckedChangeListener(onReceiverTypeCheckedChangeListener);

        mFlightModeModel1 = (FlightModeModel) v.findViewById(R.id.flight_mode_1);
        mFlightModeModel2 = (FlightModeModel) v.findViewById(R.id.flight_mode_2);
        mFlightModeModel3 = (FlightModeModel) v.findViewById(R.id.flight_mode_3);
        mFlightModeModel4 = (FlightModeModel) v.findViewById(R.id.flight_mode_4);
        mFlightModeModel5 = (FlightModeModel) v.findViewById(R.id.flight_mode_5);
        mFlightModeModel6 = (FlightModeModel) v.findViewById(R.id.flight_mode_6);

        mFlightModeModel1.registerFlightModeModelButtonClickListener(R.id.flight_mode_1, flightModeModelButtonClickListener);
        mFlightModeModel2.registerFlightModeModelButtonClickListener(R.id.flight_mode_2, flightModeModelButtonClickListener);
        mFlightModeModel3.registerFlightModeModelButtonClickListener(R.id.flight_mode_3, flightModeModelButtonClickListener);
        mFlightModeModel4.registerFlightModeModelButtonClickListener(R.id.flight_mode_4, flightModeModelButtonClickListener);
        mFlightModeModel5.registerFlightModeModelButtonClickListener(R.id.flight_mode_5, flightModeModelButtonClickListener);
        mFlightModeModel6.registerFlightModeModelButtonClickListener(R.id.flight_mode_6, flightModeModelButtonClickListener);

        ((RadioGroup) v.findViewById(R.id.settings_tuning_radio_group)).setOnCheckedChangeListener(onTuningCheckedChangeListener);

        ((ToggleButton) v.findViewById(R.id.gear_reverse_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Logger.d("*****Settings - Gear Reverse: ON");
                } else {
                    Logger.d("*****Settings - Gear Reverse: OFF");
                }
            }
        });

        ((ToggleButton) v.findViewById(R.id.camera_trigger_reverse_button)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Logger.d("*****Settings - Camera Trigger Reverse: ON");
                } else {
                    Logger.d("*****Settings - Camera Trigger Reverse: OFF");
                }
            }
        });

        v.findViewById(R.id.rc_calibration_start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.d("*****Settings - Calibration START");
            }
        });

    }

    private RadioGroup.OnCheckedChangeListener onReceiverTypeCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.receiver_type_futaba_button:
                    Logger.d("*****Settings - ReceiverType: Futaba");
                    break;
                case R.id.receiver_type_jr_button:
                    Logger.d("*****Settings - ReceiverType: JR");
                    break;
                case R.id.receiver_type_spektrum_button:
                    Logger.d("*****Settings - ReceiverType: Spektrum");
                    break;
            }
        }
    };

    private FlightModeModel.FlightModeModelButtonClickListener flightModeModelButtonClickListener = new FlightModeModel.FlightModeModelButtonClickListener() {
        @Override
        public void onModeTypeButtonClick(int resourceId, int[] viewLocation) {
            showFlightModeTypePopupDialog(resourceId, viewLocation);
        }

        @Override
        public void onFlightModeLockTypeCheck(int resourceId, int type) {
            switch (resourceId) {
                case R.id.flight_mode_1:
                    Logger.d("*****Settings - FlightMode1: " + type);
                    break;
                case R.id.flight_mode_2:
                    Logger.d("*****Settings - FlightMode2: " + type);
                    break;
                case R.id.flight_mode_3:
                    Logger.d("*****Settings - FlightMode3: " + type);
                    break;
                case R.id.flight_mode_4:
                    Logger.d("*****Settings - FlightMode4: " + type);
                    break;
                case R.id.flight_mode_5:
                    Logger.d("*****Settings - FlightMode5: " + type);
                    break;
                case R.id.flight_mode_6:
                    Logger.d("*****Settings - FlightMode6: " + type);
                    break;
            }
        }
    };

    private void showFlightModeTypePopupDialog(final int resourceId, int[] viewLocation) {
        mFlightModeTypePopupDialog = new Dialog(getActivity());
        mFlightModeTypePopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mFlightModeTypePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFlightModeTypePopupDialog.setContentView(R.layout.popwindow_flight_mode_type);
        WindowManager.LayoutParams wmlp = mFlightModeTypePopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = viewLocation[0] - getResources().getDimensionPixelOffset(R.dimen.popdialog_more_function_width);
        wmlp.y = viewLocation[1];
        mFlightModeTypePopupDialog.getWindow().setAttributes(wmlp);
        mFlightModeTypePopupDialog.show();

        mFlightModeTypePopupDialog.findViewById(R.id.altitude_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.manual_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.gps_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.rtl_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.land_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);
        mFlightModeTypePopupDialog.findViewById(R.id.optical_flow_button).setOnClickListener(onFlightModeTypePopDialogButtonClickListener);

        mFlightModeTypePopupDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mFlightModeTypeSettingValue == null || mFlightModeTypeSettingValue.isEmpty()) {
                    return;
                }
                switch (resourceId) {
                    case R.id.flight_mode_1:
                        mFlightModeModel1.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode1: " + mFlightModeTypeSettingValue);
                        break;
                    case R.id.flight_mode_2:
                        mFlightModeModel2.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode2: " + mFlightModeTypeSettingValue);
                        break;
                    case R.id.flight_mode_3:
                        mFlightModeModel3.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode3: " + mFlightModeTypeSettingValue);
                        break;
                    case R.id.flight_mode_4:
                        mFlightModeModel4.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode4: " + mFlightModeTypeSettingValue);
                        break;
                    case R.id.flight_mode_5:
                        mFlightModeModel5.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode5: " + mFlightModeTypeSettingValue);
                        break;
                    case R.id.flight_mode_6:
                        mFlightModeModel6.setModeTypeButtonText(mFlightModeTypeSettingValue);
                        Logger.d("*****Settings - FlightMode6: " + mFlightModeTypeSettingValue);
                        break;
                }
                mFlightModeTypeSettingValue = "";
            }
        });
    }

    private View.OnClickListener onFlightModeTypePopDialogButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.altitude_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.flight_altitude);
                    break;
                case R.id.manual_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.manual);
                    break;
                case R.id.gps_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.gps);
                    break;
                case R.id.rtl_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.rtl);
                    break;
                case R.id.land_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.land);
                    break;
                case R.id.optical_flow_button:
                    mFlightModeTypeSettingValue = getResources().getString(R.string.optical_flow);
                    break;
            }
            mFlightModeTypePopupDialog.dismiss();
        }
    };

    private RadioGroup.OnCheckedChangeListener onTuningCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.tuning_throttle_button:
                    Logger.d("*****Settings - Tuning: " + getResources().getString(R.string.throttle_position_for_hovering));
                    break;
                case R.id.tuning_basic_button:
                    Logger.d("*****Settings - Tuning: " + getResources().getString(R.string.basic_p_gain_roll_pitch));
                    break;
            }
        }
    };
}
