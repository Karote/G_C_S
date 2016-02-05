package com.coretronic.drone.missionplan.fragments;

import com.coretronic.drone.model.Mission.Type;

/**
 * Created by Poming on 2015/10/16.
 */
public interface SelectedMissionUpdatedCallback {

    void onMissionAltitudeUpdate(float newValue);

    void onMissionStayUpdate(int seconds);

    void onMissionTypeUpdate(Type type);

    void onMissionLongitudeUpdate(float longitude);

    void onMissionLatitudeUpdate(float latitude);

    void onMissionDeleted();

    void onMissionSpeedUpdate(int speed);
}
