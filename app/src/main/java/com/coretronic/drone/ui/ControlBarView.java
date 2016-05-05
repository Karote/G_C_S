package com.coretronic.drone.ui;

import android.view.View;
import android.view.View.OnClickListener;

import com.coretronic.drone.R;

/**
 * Created by Poming on 2015/10/28.
 */
public class ControlBarView {
    private final View mControlMainPanel;
    private final View mDroneControlBar;
    private final View mMapControlBar;
    private final View mPlanGoButton;
    private final View mPlanStopButton;
    private final View mDroneTakeoffButton;
    private final View mDroneLandingButton;
    private final View mPlanPlayButton;
    private final View mPlanPauseButton;
    private final View mFPVAndMapSwitchButton;

    public ControlBarView(View view, int controlBarId, OnClickListener onClickListener) {

        mControlMainPanel = view.findViewById(controlBarId);

        mDroneControlBar = mControlMainPanel.findViewById(R.id.drone_control_bar);
        mPlanGoButton = mDroneControlBar.findViewById(R.id.plan_go_button);
        mPlanGoButton.setOnClickListener(onClickListener);
        mPlanStopButton = mDroneControlBar.findViewById(R.id.plan_stop_button);
        mPlanStopButton.setOnClickListener(onClickListener);
        mDroneTakeoffButton = mDroneControlBar.findViewById(R.id.drone_takeoff_button);
        if (mDroneTakeoffButton != null) {
            mDroneTakeoffButton.setOnClickListener(onClickListener);
        }
        mDroneLandingButton = mDroneControlBar.findViewById(R.id.drone_landing_button);
        mDroneLandingButton.setOnClickListener(onClickListener);
        mPlanPlayButton = mDroneControlBar.findViewById(R.id.plan_play_button);
        if (mPlanPlayButton != null) {
            mPlanPlayButton.setOnClickListener(onClickListener);
        }
        mPlanPauseButton = mDroneControlBar.findViewById(R.id.plan_pause_button);
        if (mPlanPauseButton != null) {
            mPlanPauseButton.setOnClickListener(onClickListener);
        }
        mPlanPauseButton.setEnabled(false);
        mDroneControlBar.findViewById(R.id.drone_rtl_button).setOnClickListener(onClickListener);

        mMapControlBar = mControlMainPanel.findViewById(R.id.map_control_bar);
        mMapControlBar.findViewById(R.id.my_location_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.drone_location_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.fit_map_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.map_type_button).setOnClickListener(onClickListener);

        mFPVAndMapSwitchButton = mControlMainPanel.findViewById(R.id.map_fpv_switch_btn);
        mFPVAndMapSwitchButton.setOnClickListener(onClickListener);

    }

    public void setVisibility(int visibility) {
        mControlMainPanel.setVisibility(visibility);
    }

    public void setDroneControlBarVisibility(int visibility) {
        mDroneControlBar.setVisibility(visibility);
    }

    public void showStopButton() {
        mPlanStopButton.setVisibility(View.VISIBLE);
        mPlanGoButton.setVisibility(View.GONE);
    }

    public void setStopButtonEnable(boolean isEnable){
        mPlanStopButton.setEnabled(isEnable);
    }

    public void showGoButton() {
        mPlanStopButton.setVisibility(View.GONE);
        mPlanGoButton.setVisibility(View.VISIBLE);
    }

    public void setGoButtonEnable(boolean isEnable){
        mPlanGoButton.setEnabled(isEnable);
    }

    public void showLandingButton() {
        mDroneLandingButton.setVisibility(View.VISIBLE);
        mDroneTakeoffButton.setVisibility(View.GONE);
    }

    public void showTakeoffButton() {
        mDroneLandingButton.setVisibility(View.GONE);
        mDroneTakeoffButton.setVisibility(View.VISIBLE);
    }

    public void showPauseButton() {
        mPlanPauseButton.setVisibility(View.VISIBLE);
        mPlanPlayButton.setVisibility(View.GONE);
    }

    public void setPauseButtonEnable(boolean isEnable){
        mPlanPauseButton.setEnabled(isEnable);
    }

    public void showPlayButton() {
        mPlanPauseButton.setVisibility(View.GONE);
        mPlanPlayButton.setVisibility(View.VISIBLE);
    }

    public void onFPVHided() {
        mFPVAndMapSwitchButton.setSelected(false);
        mMapControlBar.setVisibility(View.VISIBLE);
    }

    public void onFPVShowed() {
        mFPVAndMapSwitchButton.setSelected(true);
        mMapControlBar.setVisibility(View.GONE);
    }
}
