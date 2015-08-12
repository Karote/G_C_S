package com.coretronic.drone.missionplan.fragments.module;

/**
 * Created by Morris on 15/8/6.
 */
public class DroneInfo {

    private long timeStamp;
    private float altitude;
    private float groundSpeed;
    private long lat;
    private long lon;
    private int eph;
    private int heading;
    private int rssi;
    private int batter;

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public void setGroundSpeed(float groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public void setLat(long lat) {
        this.lat = lat;
    }

    public void setLon(long lon) {
        this.lon = lon;
    }

    public void setEph(int eph) {
        this.eph = eph;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setBatter(int batter) {
        this.batter = batter;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getAltitude() {
        return altitude;
    }

    public float getGroundSpeed() {
        return groundSpeed;
    }

    public long getLat() {
        return lat;
    }

    public long getLon() {
        return lon;
    }

    public int getEph() {
        return eph;
    }

    public int getHeading() {
        return heading;
    }

    public int getRssi() {
        return rssi;
    }

    public int getBatter() {
        return batter;
    }
}
