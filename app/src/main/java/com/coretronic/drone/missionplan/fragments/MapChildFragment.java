package com.coretronic.drone.missionplan.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.coretronic.drone.missionplan.map.OnMapEventCallback;

/**
 * Created by karot.chuang on 2015/7/23.
 */
public class MapChildFragment extends Fragment implements OnMapEventCallback {

    protected MapViewFragment mMapViewFragment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapViewFragment = (MapViewFragment) getParentFragment();
    }

    public void onClick(View v) {
    }

    @Override
    public void onMapClickEvent(float lat, float lon) {

    }

    @Override
    public void onMapDragEndEvent(int index, float lat, float lon) {

    }

    @Override
    public void onMapPolylineLengthCalculated(int lengthInMeters) {

    }

    @Override
    public void onMapDragStartEvent() {

    }

    @Override
    public void onMapDeleteMarker(int index) {

    }

    @Override
    public void onGetMissionPlanPathDistanceAndFlightTimeCallback(int lengthInMeters, int timeInSeconds) {

    }

    public void onFPVShowed() {

    }

    public void onFPVHided() {

    }

}
