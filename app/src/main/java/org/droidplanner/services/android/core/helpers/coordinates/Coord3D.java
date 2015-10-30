package org.droidplanner.services.android.core.helpers.coordinates;

public class Coord3D extends Coord2D {
    private double alt;

    public Coord3D(double lat, double lon, double alt) {
        super(lat, lon);
        this.alt = alt;
    }

    public Coord3D(Coord2D point, double alt) {
        this(point.getLatitude(), point.getLongitude(), alt);
    }

    public void set(double lat, double lon, double alt) {
        super.set(lat, lon);
        this.alt = alt;
    }

    public double getAltitude() {
        return alt;
    }

    @Override
    public String toString() {
        return "lat/lon/alt: " + getLatitude() + "/" + getLongitude() + "/" + alt;
    }
}
