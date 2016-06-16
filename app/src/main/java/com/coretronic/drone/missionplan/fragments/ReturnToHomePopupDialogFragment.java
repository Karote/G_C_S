package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.util.AppConfig;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by karot.chuang on 2016/4/12.
 */
public class ReturnToHomePopupDialogFragment extends Fragment {

    private AbstractWheel mRTLAltitudeWheel;
    private AbstractWheel mRTLStayWheel;
    private AbstractWheel mRTLSpeedWheel;

    private int mRTLAltitude = ConstantValue.ALTITUDE_DEFAULT_VALUE;
    private int mRTLStay = ConstantValue.STAY_DEFAULT_VALUE;
    private int mRTLSpeed = ConstantValue.SPEED_DEFAULT_VALUE;

    private PopupDialogCallback mPopupDialogCallback;

    private SharedPreferences mSharedPreferences;

    public interface PopupDialogCallback {
        void onCancelButtonClick();

        void onConfirmButtonClick(float altitude, float stay, float speed);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.popup_dialog_rtl, container, false);
        findViews(fragmentView);
        return fragmentView;
    }

    public void setPopDialogCallbackListener(PopupDialogCallback popDialogCallbackListener) {
        this.mPopupDialogCallback = popDialogCallbackListener;
    }

    private void findViews(View fragmentView) {

        mRTLAltitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.rtl_altitude_wheel);
        mRTLAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number_for_one_key_popup_dialog, ConstantValue.ALTITUDE_MIN_VALUE, ConstantValue.ALTITUDE_MAX_VALUE, "%01d"));
        mRTLAltitudeWheel.setCyclic(false);
        mRTLAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
        mRTLAltitudeWheel.setCurrentItem(mRTLAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
        mRTLAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mRTLAltitude = newValue - ConstantValue.ALTITUDE_MIN_VALUE;
            }
        });

        mRTLStayWheel = (AbstractWheel) fragmentView.findViewById(R.id.rtl_stay_wheel);
        mRTLStayWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number_for_one_key_popup_dialog, ConstantValue.STAY_MIN_VALUE, ConstantValue.STAY_MAX_VALUE, "%01d"));
        mRTLStayWheel.setCyclic(false);
        mRTLStayWheel.setCurrentItem(ConstantValue.STAY_DEFAULT_VALUE - ConstantValue.STAY_MIN_VALUE);
        mRTLStayWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mRTLStay = newValue - ConstantValue.STAY_MIN_VALUE;
            }
        });

        mRTLSpeedWheel = (AbstractWheel) fragmentView.findViewById(R.id.rtl_speed_wheel);
        mRTLSpeedWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number_for_one_key_popup_dialog, ConstantValue.SPEED_MIN_VALUE, ConstantValue.SPEED_MAX_VALUE, "%01d"));
        mRTLSpeedWheel.setCyclic(false);
        mRTLSpeed = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, ConstantValue.SPEED_DEFAULT_VALUE);
        mRTLSpeedWheel.setCurrentItem(mRTLSpeed - ConstantValue.SPEED_MIN_VALUE);
        mRTLSpeedWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mRTLSpeed = newValue - ConstantValue.SPEED_MIN_VALUE;
            }
        });

        fragmentView.findViewById(R.id.rtl_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDialogCallback.onCancelButtonClick();

                mRTLAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
                mRTLAltitudeWheel.setCurrentItem(mRTLAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
                mRTLSpeed = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, ConstantValue.SPEED_DEFAULT_VALUE);
                mRTLSpeedWheel.setCurrentItem(mRTLSpeed - ConstantValue.SPEED_MIN_VALUE);
            }
        });

        fragmentView.findViewById(R.id.rtl_confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDialogCallback.onConfirmButtonClick(mRTLAltitude, mRTLStay, mRTLSpeed);

                mRTLAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
                mRTLAltitudeWheel.setCurrentItem(mRTLAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
                mRTLSpeed = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_HORIZONTAL_SPEED_DEFAULT_FOR_WAYPOINT, ConstantValue.SPEED_DEFAULT_VALUE);
                mRTLSpeedWheel.setCurrentItem(mRTLSpeed - ConstantValue.SPEED_MIN_VALUE);
            }
        });
    }
}
