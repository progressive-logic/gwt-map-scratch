package uk.co.plogic.gwt.client;

import uk.co.plogic.gwt.lib.jso.PageVariables;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.MapOptions;
import com.google.maps.gwt.client.MapTypeId;
import com.google.maps.gwt.client.Marker;
import com.google.maps.gwt.client.Marker.ClickHandler;
import com.google.maps.gwt.client.MarkerOptions;
import com.google.maps.gwt.client.MouseEvent;


public class AnimatedMarker implements EntryPoint {

	protected GoogleMap gMap;


	public class MarkerMoveAnimation  extends Animation {

		Marker mapMarker;
		LatLng startPosition;
		double lat_diff, lng_diff;

		public MarkerMoveAnimation(Marker mapMarker, LatLng startPosition, LatLng endPosition) {
			this.mapMarker = mapMarker;
			this.startPosition = startPosition;
			
			lat_diff = endPosition.lat() - startPosition.lat();
			lng_diff = endPosition.lng() - startPosition.lng();
		}

		@Override
		protected void onUpdate(double progress) {
			double lat = startPosition.lat() + (lat_diff*progress);
			double lng = startPosition.lng() + (lng_diff*progress);
			LatLng position = LatLng.create(lat, lng);
			mapMarker.setPosition(position);
		}
		
	}

	@Override
	public void onModuleLoad() {

		PageVariables pv = getPageVariables();

		MapOptions myOptions = MapOptions.create();
	    myOptions.setZoom(Double.parseDouble(pv.getStringVariable("ZOOM")));
	    LatLng myLatLng = LatLng.create(Double.parseDouble(pv.getStringVariable("LAT")),
	    								Double.parseDouble(pv.getStringVariable("LNG"))
	    								);
	    myOptions.setCenter(myLatLng);
	    myOptions.setMapTypeId(MapTypeId.ROADMAP);

	    gMap = GoogleMap.create(Document.get().getElementById(pv.getStringVariable("DOM_MAP_DIV")),
	    													  myOptions);
	    
	    final LatLng cheltenham = LatLng.create(51.91716758909015, -2.0775318145751953);
	    final LatLng enfield = LatLng.create(51.66233415804707, -0.07802009582519531);
	    
	    MarkerOptions options = MarkerOptions.create();
	    options.setPosition(cheltenham);
		options.setMap(gMap);
	
		final Marker mapMarker = Marker.create(options);
		mapMarker.addClickListener(new ClickHandler() {

			@Override
			public void handle(MouseEvent event) {
				//mapMarker.setPosition(enfield);
				
				MarkerMoveAnimation ma = new MarkerMoveAnimation(mapMarker, cheltenham, enfield);
				ma.run(500);
			}
			
		});
//	    gMap.addClickListener(new ClickHandler() {
//			@Override
//			public void handle(MouseEvent event) {
//				System.out.println("click:"+event.getLatLng());
//			}
//		});

	}

    private native PageVariables getPageVariables() /*-{
		return $wnd["config"];
	}-*/;
}
