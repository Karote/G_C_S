package com.coretronic.drone.missionplan.map;

public interface OnMapEventCallback {

    void onClick(float lat, float lon);

    void onDragEnd(int index, float lat, float lon);

    void onPolylineLengthCalculated(int lengthInMeters);

    void onDragStart();
}