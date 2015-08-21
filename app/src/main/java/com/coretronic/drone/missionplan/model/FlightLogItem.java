package com.coretronic.drone.missionplan.model;

import com.coretronic.drone.missionplan.fragments.module.DroneInfo;
import com.coretronic.drone.model.Mission;

import java.util.List;

/**
 * Created by karot.chuang on 2015/8/5.
 */
public class FlightLogItem {
    private List<Mission> mMissionList = null;
    private List<DroneInfo> mPathList = null;

    public FlightLogItem(List<Mission> missionList, List<DroneInfo> pathList) {
        mMissionList = missionList;
        mPathList = pathList;
    }

    public List<Mission> getMissionList() {
        return mMissionList;
    }

    public List<DroneInfo> getPathList() {
        return mPathList;
    }
}
