package com.coretronic.drone.missionplan.model;

import java.util.Date;
import java.util.List;

/**
 * Created by karot.chuang on 2015/8/5.
 */
public class FlightLogItem {
    private String mFlightFileName = "";
    private String mFlightDate = null;
    private List<Double> mFlightPath = null;

    public FlightLogItem(String flightFileName, String flightDate, List<Double> flightPath){
        mFlightFileName= flightFileName;
        mFlightDate = flightDate;
        mFlightPath = flightPath;
    }

    public String getFlightFileName(){
        return mFlightFileName;
    }

    public String getFlightDate(){
        return mFlightDate;
    }

    public List<Double> getFlightPath() {
        return mFlightPath;
    }
}
