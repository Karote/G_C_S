package com.coretronic.drone.missionplan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karot.chuang on 2015/5/15.
 */
public class Waypoint {
    private double latitude;
    private double longitude;
    private double altitude;

    public Waypoint(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public void set(Waypoint update) {
        this.latitude = update.latitude;
        this.longitude = update.longitude;
        this.altitude = update.altitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public double getAltitude(){
        return this.altitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude){
        this.altitude = altitude;
    }
}
