package org.droidplanner.services.android.core.survey.grid;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.geoTools.PolylineTools;

import java.util.List;

public class Grid {
    private List<Coord2D> gridPoints;
    private List<Coord2D> cameraLocations;

    public Grid(List<Coord2D> list, List<Coord2D> cameraLocations) {
        this.gridPoints = list;
        this.cameraLocations = cameraLocations;
    }

    public double getLength() {
        return PolylineTools.getPolylineLength(gridPoints);
    }

    public int getNumberOfLines() {
        return gridPoints.size() / 2;
    }

    public List<Coord2D> getCameraLocations() {
        return cameraLocations;
    }

    public int getCameraCount() {
        return cameraLocations.size();
    }

    public List<Coord2D> getGridPoints() {
        return gridPoints;
    }

    @Override
    public String toString() {
        return "Grid{" +
                "gridPoints=" + gridPoints +
                ", cameraLocations=" + cameraLocations +
                '}';
    }
}