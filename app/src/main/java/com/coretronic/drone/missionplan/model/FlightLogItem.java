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
    private String mFlightTime = "";

    public FlightLogItem(String flightFileName, String flightDate, List<Double> flightPath, String flightTime){
        mFlightFileName= flightFileName;
        mFlightDate = flightDate;
        mFlightPath = flightPath;
        mFlightTime = flightTime;
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

    public String getFlightTime(){
        return mFlightTime;
    }
}
