package uk.co.plogic.gwt.lib.map.markers;

import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;

import uk.co.plogic.gwt.lib.map.overlay.AbstractOverlay;
import uk.co.plogic.gwt.lib.utils.StringUtils;

public abstract class AbstractBaseMarker implements BaseMarker {

	protected GoogleMap gmap;
	protected String uniqueIdentifier;
	protected AbstractOverlay overlay; // a marker belongs to 0 or 1 overlays

	public enum UserInteraction { CLICK, MOUSEOVER, MOUSEOUT, MOUSEMOVE,
	                              USER_UPDATED
	                            }

	public AbstractBaseMarker(String Id) {
		setId(Id);
	}

	public String getId() { return uniqueIdentifier; }

	public void setId(String id) {
		if( ! StringUtils.legalIdString(id))
			throw new IllegalArgumentException("Id=["+id+"] not allowed. Alphanumeric, _ & - & : only for all IDs");

		uniqueIdentifier = id;
	}

	public void setMap(GoogleMap gMap) { gmap = gMap; }
	public void remove() {
		hide();
		gmap = null;
	}

	public void setOverlay(AbstractOverlay overlay) { this.overlay = overlay; }
	public AbstractOverlay getOverlay() { return overlay; }

	/**
	 * Marker tells parent of user interaction. The marker could also use the
	 * eventHandler to tell everyone.
	 * @param latLng - where the interaction occurred. i.e. could be anywhere in
	 * 				   a polygon.
	 */
	protected void relayUserAction(UserInteraction ui, LatLng latLng) {

		// markers don't need to belong to an overlay
		if(overlay == null)
			return;

		overlay.userInteractionWithMarker(ui, getId(), latLng);
	}

}