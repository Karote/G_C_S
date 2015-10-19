package org.droidplanner.services.android.core.helpers.coordinates;

import org.droidplanner.services.android.core.helpers.geoTools.GeoTools;

import java.util.List;

/**
 * Calculate a rectangle that bounds all inserted points
 */
public class CoordBounds {
	public Coord2D sw_3quadrant;
	public Coord2D ne_1quadrant;

	public CoordBounds(Coord2D point) {
		include(point);
	}

	public CoordBounds(List<Coord2D> points) {
		for (Coord2D point : points) {
			include(point);
		}
	}

	public void include(Coord2D point) {
		if ((sw_3quadrant == null) || (ne_1quadrant == null)) {
			ne_1quadrant = new Coord2D(point);
			sw_3quadrant = new Coord2D(point);
		} else {
			if (point.getY() > ne_1quadrant.getY()) {
				ne_1quadrant.set(ne_1quadrant.getX(), point.getY());
			}
			if (point.getX() > ne_1quadrant.getX()) {
				ne_1quadrant.set(point.getX(), ne_1quadrant.getY());
			}
			if (point.getY() < sw_3quadrant.getY()) {
				sw_3quadrant.set(sw_3quadrant.getX(), point.getY());
			}
			if (point.getX() < sw_3quadrant.getX()) {
				sw_3quadrant.set(point.getX(), sw_3quadrant.getY());
			}
		}
	}

	public double getDiag() {
		return GeoTools.latToMeters(GeoTools.getAproximatedDistance(ne_1quadrant, sw_3quadrant));
	}

	public Coord2D getMiddle() {
		return (new Coord2D((ne_1quadrant.getLatitude() + sw_3quadrant.getLatitude()) / 2,
				(ne_1quadrant.getLongitude() + sw_3quadrant.getLongitude()) / 2));

	}
}
