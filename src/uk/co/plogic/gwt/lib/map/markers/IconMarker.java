package uk.co.plogic.gwt.lib.map.markers;

import uk.co.plogic.gwt.lib.events.MapMarkerClickEvent;
import uk.co.plogic.gwt.lib.events.MouseOutMapMarkerEvent;
import uk.co.plogic.gwt.lib.events.MouseOverMapMarkerEvent;

import com.google.gwt.event.shared.HandlerManager;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.Marker.ClickHandler;
import com.google.maps.gwt.client.Marker.DragEndHandler;
import com.google.maps.gwt.client.MarkerOptions;
import com.google.maps.gwt.client.MarkerImage;
import com.google.maps.gwt.client.MouseEvent;


public class IconMarker extends AbstractBaseMarker implements PointMarker, EdittableMarker {

	protected Marker mapMarker;
	protected boolean editMode = false;
	protected String title = null;

	public IconMarker(	final HandlerManager eventBus, String uniqueIdentifier,
						final MarkerImage markerIcon, LatLng coord, String title ) {

		super(uniqueIdentifier);

		MarkerOptions options = MarkerOptions.create();
		//options.setTitle(bp.getTitle());
		//options.setTitle(bp.getId());
		if( markerIcon != null )
			options.setIcon(markerIcon);

		options.setPosition(coord);

		this.title = title;
		if( title != null )
		    options.setTitle(title);

		mapMarker = Marker.create(options);
		final IconMarker thisMapPointMarker = this;
		mapMarker.addClickListener(new ClickHandler() {
			@Override
			public void handle(MouseEvent event) {
				//System.out.println("click:"+event.getLatLng());
				eventBus.fireEvent(new MapMarkerClickEvent(thisMapPointMarker));
				relayUserAction(UserInteraction.CLICK, mapMarker.getPosition());
			}
		});
		mapMarker.addMouseOverListener(new Marker.MouseOverHandler() {
			@Override
			public void handle(MouseEvent event) {
				eventBus.fireEvent(new MouseOverMapMarkerEvent(thisMapPointMarker));
			}
		});
		mapMarker.addMouseOutListener(new Marker.MouseOutHandler() {
			@Override
			public void handle(MouseEvent event) {
				eventBus.fireEvent(new MouseOutMapMarkerEvent(thisMapPointMarker));
			}
		});
		mapMarker.addDragEndListener(new DragEndHandler() {

            @Override
            public void handle(MouseEvent event) {
                relayUserAction(UserInteraction.USER_UPDATED, mapMarker.getPosition());
            }

		});

	}

	@Override
	public void setMap(GoogleMap gMap) {
		super.setMap(gMap);
		mapMarker.setMap(gmap);
	}

	public Marker getMapMarker() {
		return mapMarker;
	}

	public void setIcon(MarkerImage icon) {
		mapMarker.setIcon(icon);
	}

	/**
	 * clear marker from map
	 */
	public void hide() {
		// Not totally sure if this is enough but JS API seems to look
		// like it is
		mapMarker.setMap((GoogleMap) null);
	}

	public void show() {
		mapMarker.setMap(gmap);
	}

	public LatLng getPosition() {
		return mapMarker.getPosition();
	}

	public void setPosition(LatLng position) {
		mapMarker.setPosition(position);
	}

    @Override
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        mapMarker.setDraggable(editMode);
    }

    public String getTitle() {
        return title;
    }

}