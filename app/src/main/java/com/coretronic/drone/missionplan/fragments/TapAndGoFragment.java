package com.coretronic.drone.missionplan.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coretronic.drone.R;
import com.coretronic.drone.model.Mission;
import com.coretronic.drone.model.Mission.Builder;
import com.coretronic.drone.model.Mission.Type;
import com.coretronic.drone.util.AppConfig;
import com.coretronic.drone.util.ConstantValue;

/**
 * Created by karot.chuang on 2015/7/21.
 */
public class TapAndGoFragment extends MapChildFragment {

    private final static boolean DEFAULT_AUTO_CONTINUE = true;
    private final static int DEFAULT_WAIT_SECONDS = 0;
    private final static int DEFAULT_RADIUS = 0;
    private final static Type DEFAULT_TYPE = Type.WAY_POINT;
    private Mission.Builder mMissionBuilder;
    private int mLastAlt = 0;
    private float mLastLat, mLastLng;

    private SharedPreferences mSharedPreferences;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int defaultAlt = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
        mMissionBuilder = new Builder();
        mMissionBuilder.setAltitude(defaultAlt).setType(DEFAULT_TYPE).setAutoContinue(DEFAULT_AUTO_CONTINUE)
                .setWaitSeconds(DEFAULT_WAIT_SECONDS).setRadius(DEFAULT_RADIUS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mission_plan_tap_and_go, container, false);
    }

    public void executeTapAndGoMission(float lat, float lng, float alt) {
        if (mDroneController != null) {
            mDroneController.moveToLocation(lat, lng, alt);
            mLastAlt = (int) alt;
            mLastLat = lat;
            mLastLng = lng;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plan_play_button:
                if(mDroneController != null) {
                    mDroneController.resumeTapAndGo(mLastLat, mLastLng, mLastAlt);
                }
                return;
            case R.id.plan_stop_button:
                if (mDroneController != null) {
                    mDroneController.stopTapAndGo();
                }
                return;
            case R.id.plan_pause_button:
                if (mDroneController != null) {
                    mDroneController.pauseTapAndGo();
                }
                return;
            default:
                break;
        }
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        int defaultAlt = mSharedPreferences.getInt(AppConfig.SHARED_PREFERENCE_ALTITUDE_DEFAULT_FOR_WAYPOINT, ConstantValue.ALTITUDE_DEFAULT_VALUE);
        mLastAlt = mLastAlt == 0 ? defaultAlt : mLastAlt;
        TapAndGoDialogFragment tapAndGoDialogFragment = TapAndGoDialogFragment.newInstance(mLastAlt, lat, lon);
        fragmentTransaction
                .replace(R.id.tap_and_go_container, tapAndGoDialogFragment, TapAndGoDialogFragment.class.getSimpleName())
                .commit();
    }

    public void clearTapMarker() {
        mMapViewFragment.clearTapMarker();
    }
}
