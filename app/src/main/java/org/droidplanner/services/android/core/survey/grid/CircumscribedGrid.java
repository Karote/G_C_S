package org.droidplanner.services.android.core.survey.grid;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.coordinates.CoordBounds;
import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;
import org.droidplanner.services.android.core.helpers.geoTools.LineCoord2D;

import java.util.ArrayList;
import java.util.List;

public class CircumscribedGrid {

    List<LineCoord2D> grid = new ArrayList<LineCoord2D>();
    private Coord2D gridLowerLeft;
    private double extrapolatedDiag;
    private double angle;
    private int lines;

    public CircumscribedGrid(List<Coord2D> polygonPoints, Double angle, Double lineDist) {
        this.angle = angle;
        findPolygonBounds(polygonPoints);
        drawGrid(lineDist);
    }

    private void drawGrid(Double lineDist) {
        int lines = 0;
        Coord2D startPoint = gridLowerLeft;
        while (lines * lineDist < extrapolatedDiag) {
            Coord2D endPoint = GeoTools.newCoordFromBearingAndDistance(startPoint, angle, extrapolatedDiag);
            LineCoord2D line = new LineCoord2D(startPoint, endPoint);
            grid.add(line);
            startPoint = GeoTools.newCoordFromBearingAndDistance(startPoint, angle + 90, lineDist);
            lines++;
        }
    }

    private void findPolygonBounds(List<Coord2D> polygonPoints) {
        CoordBounds bounds = new CoordBounds(polygonPoints);
        Coord2D middlePoint = bounds.getMiddle();
        gridLowerLeft = GeoTools.newCoordFromBearingAndDistance(middlePoint, angle - 135,
                bounds.getDiag());
        extrapolatedDiag = bounds.getDiag() * 1.5;
    }

    public List<LineCoord2D> getGrid() {
        return grid;
    }

    public int getLines() {
        return lines;
    }
}
