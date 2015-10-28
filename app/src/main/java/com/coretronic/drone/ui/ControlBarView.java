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
    private final View mDroneLandingButton;
    private final View mMissionStopButtonLayout;

    public ControlBarView(View view, int controlBarId, OnClickListener onClickListener) {

        mControlMainPanel = view.findViewById(controlBarId);

        mDroneControlBar = mControlMainPanel.findViewById(R.id.drone_control_bar);
        mPlanGoButton = mDroneControlBar.findViewById(R.id.plan_go_button);
        mPlanGoButton.setOnClickListener(onClickListener);
        mDroneLandingButton = mDroneControlBar.findViewById(R.id.drone_landing_button);
        mDroneLandingButton.setOnClickListener(onClickListener);
        mDroneControlBar.findViewById(R.id.drone_rtl_button).setOnClickListener(onClickListener);
        mDroneControlBar.findViewById(R.id.plan_stop_button).setOnClickListener(onClickListener);
        mMissionStopButtonLayout = mDroneControlBar.findViewById(R.id.plan_stop_layout);

        mMapControlBar = mControlMainPanel.findViewById(R.id.map_control_bar);
        mMapControlBar.findViewById(R.id.my_location_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.drone_location_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.fit_map_button).setOnClickListener(onClickListener);
        mMapControlBar.findViewById(R.id.map_type_button).setOnClickListener(onClickListener);

    }

    public void setMissionStopButtonVisibility(int visibility) {
        mMissionStopButtonLayout.setVisibility(visibility);
    }

    public void setVisibility(int visibility) {
        mControlMainPanel.setVisibility(visibility);
    }

    public void showLandingButton() {
        mDroneLandingButton.setVisibility(View.VISIBLE);
        mPlanGoButton.setVisibility(View.GONE);
    }

    public void showGoButton() {
        mDroneLandingButton.setVisibility(View.GONE);
        mPlanGoButton.setVisibility(View.VISIBLE);
    }

}
