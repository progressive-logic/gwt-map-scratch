package uk.co.plogic.gwt.lib.map.markers.utils;

import uk.co.plogic.gwt.lib.map.markers.PointMarker;

import com.google.gwt.animation.client.Animation;
import com.google.maps.gwt.client.LatLng;


/**
 * Linear move animation moves a marker from one position to another.
 * @author si
 *
 */
public class MarkerMoveAnimation  extends Animation {

	PointMarker mapMarker;
	LatLng startPosition;
	double lat_diff, lng_diff;

	public MarkerMoveAnimation(PointMarker mapMarker, LatLng startPosition, LatLng endPosition) {
		this.mapMarker = mapMarker;
		this.startPosition = startPosition;
		
		lat_diff = endPosition.lat() - startPosition.lat();
		lng_diff = endPosition.lng() - startPosition.lng();
		//System.out.println(""+lat_diff);
	}

	@Override
	protected void onUpdate(double progress) {
		double lat = startPosition.lat() + (lat_diff*progress);
		double lng = startPosition.lng() + (lng_diff*progress);
		LatLng position = LatLng.create(lat, lng);
		mapMarker.setPosition(position);
	}
	
}
