package com.coretronic.drone.missionplan.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.coretronic.drone.R;
import com.coretronic.drone.missionplan.spinnerWheel.AbstractWheel;
import com.coretronic.drone.missionplan.spinnerWheel.OnWheelChangedListener;
import com.coretronic.drone.missionplan.spinnerWheel.adapter.FirstNullNumericWheelAdapter;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.util.ConstantValue;

import java.util.List;

/**
 * Created by karot.chuang on 2016/2/3.
 */
public class WaypointListEditSettingDialog extends Dialog {

    private Context mContext;
    private View mClickView;
    private List<Mission> mSettingList;
    private OnSettingDialogObjectEventListener mListener;

    private View mOkButton;
    //    private Spinner mTypeSpinner = null;
//    private ImageView mTypeImageView = null;
    private AbstractWheel mAltitudeWheel = null;
    private AbstractWheel mStayWheel = null;
    private AbstractWheel mSpeedWheel = null;

    private float mAltitudeSettingValue;
    private int mStaySettingValue, mSpeedSettingValue;

    public WaypointListEditSettingDialog(Context context, View v, List<Mission> missionList) {
        super(context);
        this.mContext = context;
        this.mClickView = v;
        this.mSettingList = missionList;
    }

    public interface OnSettingDialogObjectEventListener {
        void onOkButtonClick(float altitude, int stay, int speed);
    }

    public void setDialogObjectEventListener(OnSettingDialogObjectEventListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.waypoint_edit_setting_dialog);

        WindowManager.LayoutParams wmlp = this.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        int[] viewLocationInPx = new int[2];
        mClickView.getLocationOnScreen(viewLocationInPx);
        wmlp.x = viewLocationInPx[0] - mContext.getResources().getDimensionPixelOffset(R.dimen.waypoint_edit_setting_dialog_xoffset);
        wmlp.y = viewLocationInPx[1];
        this.getWindow().setAttributes(wmlp);

        findViews();
        initViewValue(mSettingList);
    }

    private void findViews() {
        this.findViewById(R.id.waypoint_edit_setting_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mOkButton = this.findViewById(R.id.waypoint_edit_setting_ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOkButtonClick(mAltitudeSettingValue, mStaySettingValue, mSpeedSettingValue);
                dismiss();
            }
        });
//        mTypeSpinner = (Spinner) this.findViewById(R.id.waypoint_edit_setting_type_spinner);
//        mTypeImageView = (ImageView) this.findViewById(R.id.waypoint_edit_setting_type_icon);
        mAltitudeWheel = (AbstractWheel) this.findViewById(R.id.altitude_wheel);
        mStayWheel = (AbstractWheel) this.findViewById(R.id.stay_wheel);
        mSpeedWheel = (AbstractWheel) this.findViewById(R.id.speed_wheel);


        mAltitudeWheel.setViewAdapter(new FirstNullNumericWheelAdapter(mContext, R.layout.text_wheel_number, ConstantValue.ALTITUDE_MIN_VALUE, ConstantValue.ALTITUDE_MAX_VALUE, "%01d"));
        mAltitudeWheel.setCyclic(false);
        mAltitudeWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mAltitudeSettingValue = newValue + ConstantValue.ALTITUDE_MIN_VALUE - 1;
                mOkButton.setEnabled(true);
            }
        });

        mStayWheel.setViewAdapter(new FirstNullNumericWheelAdapter(mContext, R.layout.text_wheel_number, ConstantValue.STAY_MIN_VALUE, ConstantValue.STAY_MAX_VALUE, "%01d"));
        mStayWheel.setCyclic(false);
        mStayWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mStaySettingValue = newValue + ConstantValue.STAY_MIN_VALUE - 1;
                mOkButton.setEnabled(true);
            }
        });

        mSpeedWheel.setViewAdapter(new FirstNullNumericWheelAdapter(mContext, R.layout.text_wheel_number, ConstantValue.SPEED_MIN_VALUE, ConstantValue.SPEED_MAX_VALUE, "%01d"));
        mSpeedWheel.setCyclic(false);
        mSpeedWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                mSpeedSettingValue = newValue + ConstantValue.SPEED_MIN_VALUE - 1;
                mOkButton.setEnabled(true);
            }
        });
    }

    private void initViewValue(List<Mission> selectList) {
//        Mission.Type preSelectType = selectList.get(0).getType();
        float preSelectAltitude = selectList.get(0).getAltitude();
        int preSelectStay = selectList.get(0).getWaitSeconds();
        int preSelectSpeed = selectList.get(0).getSpeed();

        for (int i = 1; i < selectList.size(); i++) {
//            if (preSelectType != null && preSelectType != selectList.get(i).getType())
//                preSelectType = null;

            if (preSelectAltitude != -1 && preSelectAltitude != selectList.get(i).getAltitude())
                preSelectAltitude = -1;

            if (preSelectStay != -1 && preSelectStay != selectList.get(i).getWaitSeconds())
                preSelectStay = -1;

            if (preSelectSpeed != -1 && preSelectSpeed != selectList.get(i).getSpeed())
                preSelectSpeed = -1;
        }
        mAltitudeWheel.setCurrentItem((int) preSelectAltitude - ConstantValue.ALTITUDE_MIN_VALUE + 1);
        mStayWheel.setCurrentItem(preSelectStay - ConstantValue.STAY_MIN_VALUE + 1);
        mSpeedWheel.setCurrentItem(preSelectSpeed - ConstantValue.SPEED_MIN_VALUE + 1);

        mAltitudeSettingValue = preSelectAltitude;
        mStaySettingValue = preSelectStay;
        mSpeedSettingValue = preSelectSpeed;
        mOkButton.setEnabled(false);
    }
}
