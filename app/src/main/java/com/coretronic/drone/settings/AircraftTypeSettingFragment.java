package com.coretronic.drone.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Parameters;
import com.coretronic.ibs.drone.MavlinkLibBridge;

/**
 * Created by karot.chuang on 2016/2/19.
 */
public class AircraftTypeSettingFragment extends SettingChildFragment {
    private final static String ARGUMENT_ALL_READY = "ALL_READY";
    private final static String ARGUMENT_AIRCRAFT_TYPE = "AIRCRAFT_TYPE";


    private int mAircraftType;

    private boolean mIsAllReady = false;

    private View mView;

    private View mFrameTypeI4Button;
    private View mFrameTypeX4Button;
    private View mFrameTypeI6Button;
    private View mFrameTypeV6Button;
    private View mFrameTypeI8Button;
    private View mFrameTypeV8Button;
    private View mFrameTypeX8Button;
    private View mFrameTypeI12Button;
    private View mFrameTypeV12Button;

    public static AircraftTypeSettingFragment newInstance(MavlinkLibBridge.DroneParameter droneParameter) {
        AircraftTypeSettingFragment fragment = new AircraftTypeSettingFragment();
        Bundle args = new Bundle();

        if (droneParameter == null) {
            return fragment;
        }

        args.putBoolean(ARGUMENT_ALL_READY, droneParameter.isAllReady());

        args.putInt(ARGUMENT_AIRCRAFT_TYPE, droneParameter.getRotoType());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_aircraft_type, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        initView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViewValue();
    }

    private void initView(View v) {
        mFrameTypeI4Button = v.findViewById(R.id.frame_type_i4_button);
        mFrameTypeX4Button = v.findViewById(R.id.frame_type_x4_button);
        mFrameTypeI6Button = v.findViewById(R.id.frame_type_i6_button);
        mFrameTypeV6Button = v.findViewById(R.id.frame_type_v6_button);
        mFrameTypeI8Button = v.findViewById(R.id.frame_type_i8_button);
        mFrameTypeV8Button = v.findViewById(R.id.frame_type_v8_button);
        mFrameTypeX8Button = v.findViewById(R.id.frame_type_x8_button);
        mFrameTypeI12Button = v.findViewById(R.id.frame_type_i12_button);
        mFrameTypeV12Button = v.findViewById(R.id.frame_type_v12_button);

        mFrameTypeI4Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeX4Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeI6Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeV6Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeI8Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeV8Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeX8Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeI12Button.setOnClickListener(onFrameTypeButtonClickListener);
        mFrameTypeV12Button.setOnClickListener(onFrameTypeButtonClickListener);
    }

    private void setAllViewEnable(boolean allViewEnable) {
        mFrameTypeI4Button.setEnabled(allViewEnable);
        mFrameTypeX4Button.setEnabled(allViewEnable);
        mFrameTypeI6Button.setEnabled(allViewEnable);
        mFrameTypeV6Button.setEnabled(allViewEnable);
        mFrameTypeI8Button.setEnabled(allViewEnable);
        mFrameTypeV8Button.setEnabled(allViewEnable);
        mFrameTypeX8Button.setEnabled(allViewEnable);
        mFrameTypeI12Button.setEnabled(allViewEnable);
        mFrameTypeV12Button.setEnabled(allViewEnable);

        mView.findViewById(R.id.frame_type_i4_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_x4_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_i6_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_v6_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_i8_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_v8_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_x8_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_i12_text).setEnabled(allViewEnable);
        mView.findViewById(R.id.frame_type_v12_text).setEnabled(allViewEnable);

        float imageAlpha = allViewEnable ? 1 : 0.3f;
        mView.findViewById(R.id.frame_type_i4_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_x4_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_i6_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_v6_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_i8_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_v8_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_x8_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_i12_image).setAlpha(imageAlpha);
        mView.findViewById(R.id.frame_type_v12_image).setAlpha(imageAlpha);
    }

    private void initViewValue() {
        Bundle arguments = getArguments();

        if (arguments == null) {
            setAllViewEnable(false);
        } else {
            mIsAllReady = arguments.getBoolean(ARGUMENT_ALL_READY);

            if (!mIsAllReady) {
                setAllViewEnable(false);
                return;
            }

            mAircraftType = arguments.getInt(ARGUMENT_AIRCRAFT_TYPE);
            setFrameTypeView(mAircraftType);
        }
    }

    private View.OnClickListener onFrameTypeButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.frame_type_i4_button:
                    mAircraftType = Parameters.ROTOR_TYPE_QUADROTOR_I;
                    break;
                case R.id.frame_type_x4_button:
                    mAircraftType = Parameters.ROTOR_TYPE_QUADROTOR;
                    break;
                case R.id.frame_type_i6_button:
                    mAircraftType = Parameters.ROTOR_TYPE_HEXAROTOR_I;
                    break;
                case R.id.frame_type_v6_button:
                    mAircraftType = Parameters.ROTOR_TYPE_HEXAROTOR;
                    break;
                case R.id.frame_type_i8_button:
                    mAircraftType = Parameters.ROTOR_TYPE_OCTOROTOR_I;
                    break;
                case R.id.frame_type_v8_button:
                    mAircraftType = Parameters.ROTOR_TYPE_OCTOROTOR;
                    break;
                case R.id.frame_type_x8_button:
                    mAircraftType = Parameters.ROTOR_TYPE_DUO_QUADROTOR;
                    break;
                case R.id.frame_type_i12_button:
                    mAircraftType = Parameters.ROTOR_TYPE_DUO_HEXAROTOR_I;
                    break;
                case R.id.frame_type_v12_button:
                    mAircraftType = Parameters.ROTOR_TYPE_DUO_HEXAROTOR;
                    break;
            }
            mDroneController.setParameters(Parameters.PARAMETER_ID.PARAMETER_ID_ROTOR_TYPE.ordinal(), mAircraftType);
            setFrameTypeView(mAircraftType);
        }
    };

    private void setFrameTypeView(int frameType) {

        mFrameTypeI4Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeX4Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeI6Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeV6Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeI8Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeV8Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeX8Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeI12Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));
        mFrameTypeV12Button.setBackgroundColor(getResources().getColor(R.color.settings_button_normal_bg));

        switch (frameType) {
            case Parameters.ROTOR_TYPE_QUADROTOR:
                mFrameTypeX4Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_HEXAROTOR:
                mFrameTypeV6Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_OCTOROTOR:
                mFrameTypeV8Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_DUO_HEXAROTOR:
                mFrameTypeV12Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_DUO_QUADROTOR:
                mFrameTypeX8Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_QUADROTOR_I:
                mFrameTypeI4Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_HEXAROTOR_I:
                mFrameTypeI6Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_OCTOROTOR_I:
                mFrameTypeI8Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
            case Parameters.ROTOR_TYPE_DUO_HEXAROTOR_I:
                mFrameTypeI12Button.setBackgroundColor(getResources().getColor(R.color.primary_color_normal));
                break;
        }
    }

}
