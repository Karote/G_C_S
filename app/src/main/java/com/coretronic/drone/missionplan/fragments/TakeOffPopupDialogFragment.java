package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.NumericWheelAdapter;
import com.coretronic.drone.util.AppConfig;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by karot.chuang on 2016/4/12.
 */
public class TakeOffPopupDialogFragment extends Fragment {

    private AbstractWheel mTakeOffAltitudeWheel;

    private PopupDialogCallback mPopupDialogCallback;
    private Dialog mNumberPadPopupDialog;

    private int mTakeOffAltitude = ConstantValue.ALTITUDE_DEFAULT_VALUE;

    private SharedPreferences mSharedPreferences;

    public interface PopupDialogCallback {
        void onCancelButtonClick();

        void onOKButtonClick(int takeOffAltitude);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.popup_dialog_take_off, container, false);
        findViews(fragmentView);
        return fragmentView;
    }

    public void setPopDialogCallbackListener(PopupDialogCallback popDialogCallbackListener) {
        this.mPopupDialogCallback = popDialogCallbackListener;
    }

    private void findViews(View fragmentView) {

        mTakeOffAltitudeWheel = (AbstractWheel) fragmentView.findViewById(R.id.takeoff_altitude_wheel);
        mTakeOffAltitudeWheel.setViewAdapter(new NumericWheelAdapter(getActivity().getBaseContext(), R.layout.text_wheel_number_for_one_key_popup_dialog, ConstantValue.ALTITUDE_MIN_VALUE, ConstantValue.ALTITUDE_MAX_VALUE, "%01d"));
        mTakeOffAltitudeWheel.setCyclic(false);
        mTakeOffAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_TAKEOFF_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.TAKEOFF_ALTITUDE_DEFAULT_VALUE);
        mTakeOffAltitudeWheel.setCurrentItem(mTakeOffAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
        mTakeOffAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mTakeOffAltitude = newValue - ConstantValue.ALTITUDE_MIN_VALUE;
            }
        });

        fragmentView.findViewById(R.id.takeoff_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDialogCallback.onCancelButtonClick();
                mTakeOffAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_TAKEOFF_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.TAKEOFF_ALTITUDE_DEFAULT_VALUE);
                mTakeOffAltitudeWheel.setCurrentItem(mTakeOffAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
            }
        });

        fragmentView.findViewById(R.id.takeoff_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupDialogCallback.onOKButtonClick(mTakeOffAltitude);
                mTakeOffAltitude = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_TAKEOFF_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.TAKEOFF_ALTITUDE_DEFAULT_VALUE);
                mTakeOffAltitudeWheel.setCurrentItem(mTakeOffAltitude - ConstantValue.ALTITUDE_MIN_VALUE);
            }
        });
    }


    private void showNumberPadPopupDialog(int[] viewLocation) {
        mNumberPadPopupDialog = new Dialog(getActivity());
        mNumberPadPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mNumberPadPopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mNumberPadPopupDialog.setContentView(R.layout.number_pad);
        WindowManager.LayoutParams wmlp = mNumberPadPopupDialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.START;
        wmlp.x = viewLocation[0] - getResources().getDimensionPixelOffset(R.dimen.number_pad_width);
        wmlp.y = viewLocation[1];
        mNumberPadPopupDialog.getWindow().setAttributes(wmlp);
        mNumberPadPopupDialog.show();

        mNumberPadPopupDialog.findViewById(R.id.number_pad_ok).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_1).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_2).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_3).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_4).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_5).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_6).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_7).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_8).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_9).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_0).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_dot).setOnClickListener(onNumberPadButtonClickListener);
        mNumberPadPopupDialog.findViewById(R.id.number_pad_delete).setOnClickListener(onNumberPadButtonClickListener);

        mNumberPadPopupDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
    }

    private View.OnClickListener onNumberPadButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.number_pad_1:
                    break;
                case R.id.number_pad_2:
                    break;
                case R.id.number_pad_3:
                    break;
                case R.id.number_pad_4:
                    break;
                case R.id.number_pad_5:
                    break;
                case R.id.number_pad_6:
                    break;
                case R.id.number_pad_7:
                    break;
                case R.id.number_pad_8:
                    break;
                case R.id.number_pad_9:
                    break;
                case R.id.number_pad_0:
                    break;
                case R.id.number_pad_dot:
                    break;
                case R.id.number_pad_delete:
                    break;
                case R.id.number_pad_ok:
                    mNumberPadPopupDialog.dismiss();
                    break;
            }
        }
    };
}
