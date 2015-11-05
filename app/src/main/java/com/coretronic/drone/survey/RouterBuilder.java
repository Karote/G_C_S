package com.coretronic.drone.survey;

import android.location.Location;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.helpers.geoTools.LineCoord2D;
import org.droidplanner.services.android.core.polygon.Polygon;
import org.droidplanner.services.android.core.survey.SurveyData;
import org.droidplanner.services.android.core.survey.grid.CircumscribedGrid;
import org.droidplanner.services.android.core.survey.grid.EndpointSorter;
import org.droidplanner.services.android.core.survey.grid.Trimmer;

import java.util.List;

public class RouterBuilder {

    private final static int MAX_NUMBER_OF_LINES = 300;
    private final static int MAX_NUMBER_OF_CAMERAS = 2000;
    private final Location mStartLocation;
    private final Location mNextLocation;

    private Polygon mPolygon;
    private Coord2D mOriginPoint;
    private SurveyData mSurveyData;
    private boolean mAutoChoose;

    public RouterBuilder() {
        mSurveyData = new SurveyData();
        mSurveyData.setCameraInfo(CameraManager.PANASONIC_GH4_INNO_FLIGHT_CAMERA);
        setAutoChoose(true);
        mStartLocation = new Location("Guided Location");
        mNextLocation = new Location("Phone Location");
    }

    public SurveyRouter build() throws SurveyBuilderException {

        if (mPolygon.getPoints().size() < 3) {
            throw new InvalidPolygonException(mPolygon.getPoints().size());
        }
        if (!mAutoChoose) {
            return generate(true);
        }
        SurveyRouter orderSurveyRouter;
        SurveyRouter reverseSurveyRouter;
        SurveyBuilderException throwException = null;
        try {
            orderSurveyRouter = createRouter(false);
        } catch (GridTooManyLinesException e) {
            throwException = e;
            orderSurveyRouter = null;
        } catch (CameraPointTooManyException e) {
            throwException = e;
            orderSurveyRouter = null;
        }

        try {
            reverseSurveyRouter = createRouter(false);
        } catch (GridTooManyLinesException e) {
            throwException = e;
            reverseSurveyRouter = null;
        } catch (CameraPointTooManyException e) {
            throwException = e;
            reverseSurveyRouter = null;
        }

        if (orderSurveyRouter == null && reverseSurveyRouter == null) {
            throw throwException;
        }

        return getFitGrid(orderSurveyRouter, reverseSurveyRouter);
    }

    private SurveyRouter getFitGrid(SurveyRouter... routers) {
        double minBenchMarkValue = Double.MAX_VALUE;
        SurveyRouter chosen = null;
        for (SurveyRouter surveyRouter : routers) {
            if (getRouterBenchMark(surveyRouter) < minBenchMarkValue) {
                chosen = surveyRouter;
                minBenchMarkValue = getRouterBenchMark(surveyRouter);
            }
        }
        return chosen;
    }

    private SurveyRouter createRouter(boolean reverse) throws CameraPointTooManyException, GridTooManyLinesException {
        mStartLocation.setLatitude(mPolygon.getPoints().get(0).getLatitude());
        mStartLocation.setLongitude(mPolygon.getPoints().get(0).getLongitude());
        int nextIndex = reverse ? mPolygon.getPoints().size() - 1 : 1;
        mNextLocation.setLatitude(mPolygon.getPoints().get(nextIndex).getLatitude());
        mNextLocation.setLongitude(mPolygon.getPoints().get(nextIndex).getLongitude());
        mSurveyData.setAngle(mNextLocation.bearingTo(mStartLocation));

        setOriginPoint(mPolygon.getPoints().get(0));
        return generate(true);
    }

    private double getRouterBenchMark(SurveyRouter surveyRouter) {
        if (surveyRouter == null) {
            return Double.MAX_VALUE;
        }
        return surveyRouter.getNumberOfLines();
    }

    public RouterBuilder setAutoChoose(boolean autoChoose) {
        mAutoChoose = autoChoose;
        return this;
    }

    public RouterBuilder setPoints(List<Coord2D> points) {
        mPolygon = new Polygon();
        for (Coord2D point : points) {
            mPolygon.addPoint(point);
        }
        mSurveyData.setPolygon(mPolygon);
        return this;
    }

    public RouterBuilder setAngle(int angle) {
        mSurveyData.setAngle(angle);
        setAutoChoose(false);
        return this;
    }

    public RouterBuilder setAltitude(int altitude) {
        mSurveyData.setAltitude(altitude);
        return this;
    }

    public RouterBuilder setPolygon(Polygon polygon) {
        this.mPolygon = polygon;
        return this;
    }

    public RouterBuilder setOriginPoint(Coord2D originPoint) {
        mOriginPoint = originPoint;
        return this;
    }

    public RouterBuilder setSidelap(int sidelap) {
        mSurveyData.setSidelap(sidelap);
        return this;
    }


    public RouterBuilder setOverlap(int overlap) {
        mSurveyData.setOverlap(overlap);
        return this;
    }

    public SurveyRouter generate(boolean sort) throws GridTooManyLinesException, CameraPointTooManyException {

        double angle = mSurveyData.getAngle();
        double lineDistance = mSurveyData.getLateralPictureDistance();
        double shutterDistance = mSurveyData.getLongitudinalPictureDistance();

        List<Coord2D> polygonPoints = mPolygon.getPoints();
        CircumscribedGrid circumscribedGrid = new CircumscribedGrid(polygonPoints, angle, lineDistance);
        if (circumscribedGrid.getLines() > MAX_NUMBER_OF_LINES) {
            throw new GridTooManyLinesException(circumscribedGrid.getLines());
        }
        List<LineCoord2D> trimedGrid = new Trimmer(circumscribedGrid.getGrid(), mPolygon.getLines()).getTrimmedGrid();
        EndpointSorter gridSorter = new EndpointSorter(trimedGrid, shutterDistance);
        gridSorter.sortGrid(mOriginPoint, sort);
        if (gridSorter.getCameraLocations().size() > MAX_NUMBER_OF_CAMERAS) {
            throw new CameraPointTooManyException(gridSorter.getCameraLocations().size());
        }
        return new SurveyRouter(gridSorter, mSurveyData);
    }

    public static class GridTooManyLinesException extends SurveyBuilderException {
        public GridTooManyLinesException(int lineCounts) {
            super("Grid Line exceed " + MAX_NUMBER_OF_LINES + "/" + lineCounts);
        }
    }

    public static class CameraPointTooManyException extends SurveyBuilderException {
        public CameraPointTooManyException(int cameraPointCount) {
            super("Camera Points  exceed " + MAX_NUMBER_OF_CAMERAS + "/" + cameraPointCount);
        }
    }

    public class InvalidPolygonException extends SurveyBuilderException {
        public InvalidPolygonException(int size) {
            super("InvalidPolygon " + size);
        }
    }

    public static class SurveyBuilderException extends Exception {
        public SurveyBuilderException(String detailMessage) {
            super(detailMessage);
        }
    }

}
