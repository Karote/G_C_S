package com.coretronic.drone.missionplan.map;

public interface OnMapEventCallback {

    void onMapClickEvent(float lat, float lon);

    void onMapDragEndEvent(int index, float lat, float lon);

    void onMapPolylineLengthCalculated(int lengthInMeters);

    void onMapDragStartEvent();

    void onMapDeleteMarker(int index);
}