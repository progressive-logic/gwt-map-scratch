package uk.co.plogic.gwt.lib.ui;

import uk.co.plogic.gwt.lib.dom.DomElementByAttributeFinder;
import uk.co.plogic.gwt.lib.dom.DomParser;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;

/**
 * 
 * This is too basic to work in real life. It's just for a demo for Potato.
 * 
 * It find a hidden form with the id="add_new_post" and <INPUT..>s with
 * id="latitude" and id="longitude" and adds the given coords to them. It then
 * clears the form's "style" attribute. This is a lazy way to unhide a form
 * which previously had an element style of "display=none;"
 * 
 * All ids are hardcoded and it fails silently.
 * 
 * @author si
 *
 */
public class FormFiddle {


	/**
	 * 
	 * @param className 
	 */
	public FormFiddle(DomParser domParser, final String elementId, final double lat, final double lng) {

	    domParser.addHandler(new DomElementByAttributeFinder("id", elementId) {

	    	
	        @Override
	        public void onDomElementFound(Element element, String id) {

	        	NodeList<Element> possibleElements = element.getElementsByTagName("input");
	        	for( int i=0; i<possibleElements.getLength(); i++ ) {
	                Element item = (Element) possibleElements.getItem(i);

	                if( item.getId().equals("lat") ) {
	                	item.setAttribute("value", Double.toString(lat) );
	                }
	                if( item.getId().equals("lng") ) {
	                	item.setAttribute("value", Double.toString(lng) );
	                }

	        	}
	        	element.setAttribute("style", "");

	        }
	    });
	}

}
