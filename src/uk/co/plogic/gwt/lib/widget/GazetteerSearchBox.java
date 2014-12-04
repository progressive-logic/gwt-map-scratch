package uk.co.plogic.gwt.lib.widget;

import java.util.HashMap;
import java.util.logging.Logger;

import uk.co.plogic.gwt.lib.comms.DropBox;
import uk.co.plogic.gwt.lib.comms.GeneralJsonService;
import uk.co.plogic.gwt.lib.comms.GeneralJsonService.LetterBox;
import uk.co.plogic.gwt.lib.comms.envelope.GazetteerEnvelope;
import uk.co.plogic.gwt.lib.events.GazetteerResultsEvent;
import uk.co.plogic.gwt.lib.utils.AttributeDictionary;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class GazetteerSearchBox extends Composite implements DropBox {
	
	private FlowPanel targetPanel = new FlowPanel();
	private GeneralJsonService gjson;
	private LetterBox letterBox;
	private HandlerManager eventBus;
	private String searchTerm;
	final static int delayDuration = 350;
	private Timer requestTimer;
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private final SuggestBox suggestbox = new SuggestBox(oracle);
	final Logger logger = Logger.getLogger("GazetteerSearch");
	private HTML locationNotFound;
	// most recent results from server side Gazetteer service
	private HashMap<String, JSONObject> searchResults = new HashMap<String, JSONObject>();

	
	public GazetteerSearchBox(HandlerManager eventBus, String url) {

		/*
		<div class="input-group">
			<span class="input-group-addon">Postcode</span>
			<input id="search_text" name="search" type="text" class="form-control" placeholder="EC1A">
			<span class="input-group-btn">
				<button id="search_button" class="btn btn-default" type="button">Go!</button>
			</span>
		</div>
		*/
		this.eventBus = eventBus;
		
		// setup comms
		gjson = new GeneralJsonService(url);
		gjson.setDeliveryPoint(this);
		letterBox = gjson.createLetterBox();

	    FlowPanel searchBoxPanel = new FlowPanel();

	    searchBoxPanel.setStyleName("input-group");

	    HTML title = new HTML("Location");
	    title.setStyleName("input-group-addon");
	    searchBoxPanel.add(title);

	    suggestbox.setStyleName("form-control");
	    suggestbox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {

				if( event.getNativeKeyCode() != KeyCodes.KEY_ENTER ) {
					// onSelection() is called on enter and before onKeyUp()
					// only make request to gazetteer service when the user
					// isn't confirming the selection
					requestTimer.cancel();
					requestTimer.schedule(delayDuration);
				}
			}
		});

	    final HandlerManager evb = this.eventBus;
	    suggestbox.addSelectionHandler(new SelectionHandler<Suggestion>(){

			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				String selectedLocationString = event.getSelectedItem().getReplacementString();
				logger.info("selection for "+selectedLocationString);
				//runQuery(selectedLocationString, false);
				if( searchResults.containsKey(selectedLocationString)) {
					requestTimer.cancel();
					locationFound(searchResults.get(selectedLocationString));
					// hide the drop down. There might be a simpler way.
					oracle.clear();
					suggestbox.refreshSuggestionList();
					suggestbox.showSuggestionList();
					suggestbox.setFocus(false);

				} else {
					logger.info("not in hash: selection for:"+selectedLocationString);
					locationNotFound.setVisible(true);
				}
			}

			private void locationFound(JSONObject l) {
				//System.out.println("one result: "+l.get("name").isString().stringValue() );
				Double lat = l.get("lat").isNumber().doubleValue();
				Double lng = l.get("lng").isNumber().doubleValue();
				GazetteerResultsEvent gazetteerResult = new GazetteerResultsEvent(searchTerm, lat, lng);

				AttributeDictionary allFields = new AttributeDictionary();
				for(String key : l.keySet() ) {
					String asString = "";
					JSONString jsVal = l.get(key).isString();
					if( jsVal != null ) {
						asString = jsVal.stringValue();
						allFields.set(key, asString);
					}
					else {
						JSONNumber jsVald = l.get(key).isNumber();
						if( jsVald != null ) {
							double dVal = jsVald.doubleValue();
							allFields.set(key, dVal);
							asString = ""+dVal;
						}
					}
					logger.fine("Gazetteer result has "+key+" : "+asString);
				}
				gazetteerResult.setFieldDictionary(allFields);
				evb.fireEvent(gazetteerResult);
			}

	    });

	    searchBoxPanel.add(suggestbox);
	    // TODO, maybe id="search_text" class="form-control" type="text" placeholder="EC1A" name="search"
	    		
	    FlowPanel buttonPanel = new FlowPanel();
	    buttonPanel.setStyleName("input-group-btn");
	    Button go = new Button("Go!");
	    go.setStyleName("btn");
	    go.addStyleName("btn-default");
	    go.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				logger.info("click:"+suggestbox.getValue());
				runQuery(suggestbox.getValue(), false);
			}
		});
	    buttonPanel.add(go);
	    searchBoxPanel.add(buttonPanel);
		
		requestTimer = new Timer() {  
		    @Override
		    public void run() {
		    	runQuery(suggestbox.getValue(), true);
		    }
		};

		FlowPanel searchExtraPanel = new FlowPanel();
		locationNotFound = new HTML("Location not found!");
		locationNotFound.setVisible(false);
		locationNotFound.setStyleName("searchExtraHtml");
		searchExtraPanel.add(locationNotFound);

		targetPanel.add(searchBoxPanel);
		targetPanel.add(searchExtraPanel);

		initWidget(targetPanel);
	}

	/**
	 * 
	 * @param searchTerm
	 * @param autoSuggest - indicate to gazetteer server if a single exact
	 * 						match should be returned if possible.
	 */
	private void runQuery(String searchTerm, boolean autoSuggest) {

		logger.fine("runQuery for "+searchTerm);
		requestTimer.cancel();
		this.searchTerm = searchTerm;

		GazetteerEnvelope envelope = new GazetteerEnvelope();
    	envelope.searchTerm(searchTerm);
    	envelope.autoSuggest(autoSuggest);

		letterBox.send(envelope);
	}

	@Override
	public void onDelivery(String letterBoxName, String jsonEncodedPayload) {
		//System.out.println("ondelivery got:"+jsonEncodedPayload);
		locationNotFound.setVisible(false);

		JSONObject fullDoc = JSONParser.parseLenient(jsonEncodedPayload).isObject();
		JSONArray locations = fullDoc.get("locations").isArray();

		if( locations.size() == 0 ) {
			locationNotFound.setVisible(true);
			//eventBus.fireEvent(new GazetteerResultsEvent(null, Double.NaN, Double.NaN));
		} else {
			// one or more results
			oracle.clear();
			searchResults.clear();
			for(int i=0; i<locations.size(); i++) {
				JSONObject l = locations.get(i).isObject();
				String suggestedItem = l.get("name").isString().stringValue();
				searchResults.put(suggestedItem, l);
				//logger.info(suggestedItem);
				oracle.add(suggestedItem);
			}
			suggestbox.refreshSuggestionList();
			suggestbox.showSuggestionList();

		}

	}
}