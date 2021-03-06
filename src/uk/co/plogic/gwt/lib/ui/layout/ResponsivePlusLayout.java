package uk.co.plogic.gwt.lib.ui.layout;

import java.util.ArrayList;
import java.util.logging.Logger;

import uk.co.plogic.gwt.lib.map.GoogleMapAdapter;
import uk.co.plogic.gwt.lib.widget.mapControl.MapControl;
import uk.co.plogic.gwt.lib.widget.mapControl.MapControlPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.maps.gwt.client.GoogleMap;
import com.google.maps.gwt.client.LatLng;
import com.google.maps.gwt.client.GoogleMap.IdleHandler;

/**
 *
 * A layout controller to programatically adjust to mobile, fullscreen, iframe,
 * named div and full page layouts.
 *
 * The 3 ways for this Layout are desktop, mobile landscape and mobile portrait.
 * The info panel is on the left for desktop+landscape but at the bottom for
 * portrait.
 *
 * There are four parts-
 * - infoPanel - Adjustable side panel
 * - header - HTML - hides when in iframe or fullscreen
 * - footer - HTML
 * - map - requires a Google map
 *
 * @author si
 *
 */
public class ResponsivePlusLayout implements ProvidesResize {

	Logger logger = Logger.getLogger("ResponsivePlusLayout");
	DockLayoutPanel layoutPanel;
	RootLayoutPanel rootPanel;
	int windowWidth = -1;
	int windowHeight = -1;

	HTML header;
	HTML footer;

	ResizeLayoutPanel infoPanel; // outside container - can hold just one widget
	FlowPanel infoContent;		 // inside container, holds controls and InfoPanelContent
	HorizontalPanel iconControls;
	CarouselBasedInfoPanel infoPanelContent;
	int defaultInfoPanelSize;
	int currentInfoPanelSize;
	int previousInfoPanelSize;

	ResponsiveLayoutImageResource images;

	// panel content
	FlowPanel folderTab; // controls when info panel is visible
	FlowPanel mapPanel;
	FlowPanel mapContainer; // this' element is given to GoogleMap.create(...)
	MapControlPanel mapExtraControlsPanel;
	GoogleMap map;
	GoogleMapAdapter mapAdapter;
	boolean mapReady = false;
	boolean resizeLocked = false;

    Image expandOpen;    // arrows on folderTab when in mobile mode
    Image expandClose;
    Timer expandArrowsTimer;
    final static int expandArrowsTimerDuration = 3000;

	String responsiveMode = "unknown"; 	// 'mobile_landscape', 'mobile_portrait'
										// and 'full_version'.
										// A String instead of an enum as it makes
										// for a better relationship with
										// user defined variable (responsive_mode) used by
										// ResponsiveJso.
	ArrayList<ResponsiveElement> responsiveElements = new ArrayList<ResponsiveElement>();
										// @see addResponsiveElement()

	final int PANEL_RESIZE_PIXELS = 150; // size of jump when click the resize arrows
	final int HEADER_HEIGHT_PIXELS = 50;
	final int FOOTER_HEIGHT_PIXELS = 30;
	final double INFO_PANEL_WINDOW_PORTION = 0.5;
	final int MOBILE_THRESHOLD_PIXELS = 620;

	class ResponsiveElement {
		String target_element_id;
		String responsive_mode;
		String add_class;
		String remove_class;
	}

	public ResponsivePlusLayout() {

		rootPanel = RootLayoutPanel.get();
		images = GWT.create(ResponsiveLayoutImageResource.class);

		mapPanel = new FlowPanel();
		mapPanel.getElement().setId("map_panel");
		mapPanel.setStyleName("map_canvas");
		mapContainer = new FlowPanel();
		mapContainer.getElement().setId("map_container");
		mapContainer.setStyleName("map_canvas");
		mapPanel.add(mapContainer);

		final ResponsivePlusLayout me = this;
		Window.addResizeHandler(new ResizeHandler() {
			Timer resizeTimer = new Timer() {
				@Override
				public void run() {
					me.onResize();
				}
			};
			@Override
			public void onResize(ResizeEvent event) {
				resizeTimer.cancel();
				resizeTimer.schedule(200);
			}
		});

        expandArrowsTimer = new Timer() {
            @Override
            public void run() {
                showHideExpandArrows(false);
            }
        };
	}

	public FlowPanel getMapContainerPanel() {
		return mapContainer;
	}

	public void setHtml(String headerHtml, String footerHtml, String infoPanelHtml) {

		header = new HTML(SafeHtmlUtils.fromTrustedString(headerHtml));
		header.setStyleName("header");

		footer = new HTML(SafeHtmlUtils.fromTrustedString(footerHtml));
		footer.setStyleName("footer");

		infoPanel = new ResizeLayoutPanel();
		infoContent = new FlowPanel();
		infoPanel.add(infoContent);
		infoContent.setStyleName("info_panel");

		infoPanelContent = new CarouselBasedInfoPanel(SafeHtmlUtils.fromTrustedString(infoPanelHtml));
        ResponsiveSizing rs = new ResponsiveSizing(getInfoPanel());
        rs.setPixelAdjustments(-30, -40);
        //rs.setScaleFactor(1.00, 0.5);
        infoPanelContent.setSuperCarouselResponsiveSizing(rs);
		infoContent.add(infoPanelContent);

	}

	public void setMap(GoogleMapAdapter gma) {
		mapAdapter = gma;
		map = gma.getGoogleMap();
		map.addIdleListenerOnce(new IdleHandler() {
			@Override
			public void handle() {
				mapReady = true;
			}
		});
	}

	public void addMapControl(MapControl c) {
		if( mapExtraControlsPanel == null ) {
			// init
			mapExtraControlsPanel = new MapControlPanel();
			mapPanel.add(mapExtraControlsPanel);
		}
		mapExtraControlsPanel.addControl(c);
	}

	public MapControlPanel getMapControlPanel() {
	    return mapExtraControlsPanel;
	}

	/**
	 * wrap (replace) of an element which is within the info panel's
	 * HTML with the given widget.
	 *
	 * @param elementId
	 * @param w
	 * @return if successful
	 */
	public boolean updateInfoPanelElement(String elementId, Widget w, Boolean replace) {
		return infoPanelContent.updateElement(elementId, w, replace);
	}

	/**
	 * When the responsive mode (i.e. 'mobile'; 'full_version' etc.) changes, update
	 * these HTML elements' style class.
	 *
	 * If an element's 'responsive_mode' matches the current mode then it's 'add_class'
	 * style will be added and the 'remove_class' style will be removed. And importantly,
	 * vice versa.
	 *
	 * @param target_element_id
	 * @param responsive_mode
	 * @param add_class
	 * @param remove_class
	 */
// this should be done by looking at all widgets onResize and seeing if they have RequiresResize
//
//	public void addResponsiveElement(String target_element_id, String responsive_mode,
//									 String add_class, String remove_class) {
//
//		ResponsiveElement re = new ResponsiveElement();
//		re.target_element_id = target_element_id;
//		re.responsive_mode = responsive_mode;
//		re.add_class= add_class;
//		re.remove_class = remove_class;
//		responsiveElements.add(re);
//	}

	public void closePanel() {

	    if( responsiveMode.startsWith("mobile") ) {
	        if( currentInfoPanelSize > defaultInfoPanelSize ) {
                // is currently closed
                currentInfoPanelSize = defaultInfoPanelSize;
            } else {
                currentInfoPanelSize = 0;
            }
	    } else {
	    	previousInfoPanelSize = currentInfoPanelSize;
    		currentInfoPanelSize = 0;
	    }
	    resizeInfoPanel();
	}

	public void openPanel() {

	    if( responsiveMode.startsWith("mobile") ) {
	        /*
	         * mobile has 3 sizes, fully open, default and closed
	         */
	        if( currentInfoPanelSize < defaultInfoPanelSize ) {
	            // is currently closed
	            currentInfoPanelSize = defaultInfoPanelSize;
	        } else if( responsiveMode.equals("mobile_portrait") ) {
	            currentInfoPanelSize = windowHeight - 35;
	        } else {
	            currentInfoPanelSize = windowWidth - 35;
	        }

        } else {
            currentInfoPanelSize = previousInfoPanelSize;
        }
        resizeInfoPanel();

	}

	/**
	 * mobile mode uses folder tabs with up down arrows
	 * that only appear after first press of folder-tab icon
	 * @param b
	 */
    public void showHideExpandArrows(boolean visible) {
        expandOpen.setVisible(visible && (currentInfoPanelSize<=defaultInfoPanelSize));
        expandClose.setVisible(visible && (currentInfoPanelSize>0));

        expandArrowsTimer.cancel();
        expandArrowsTimer.schedule(expandArrowsTimerDuration);
    }


	private void resizeInfoPanel() {
		LatLng centre = null;
		if( map != null )
			centre = map.getCenter();

		layoutPanel.setWidgetSize(infoPanel, currentInfoPanelSize);
		layoutPanel.animate(250);

		final LatLng cc = centre;

        Timer resizeTimer = new Timer() {
			   @Override
			   public void run() {
				   if( cc != null && mapReady ) {
					   map.triggerResize();
					   map.setCenter(cc);
				   }
                   infoPanelContent.onResize();
                   updateFolderTabs();
			   }
         };
         // only after panel has finished changing size
         resizeTimer.schedule(250);
	}

	public void setResizeLocked(boolean resizeLocked) {
        this.resizeLocked = resizeLocked;
    }

    private void layoutAsDesktop() {
		// 40%
        defaultInfoPanelSize = (int) (windowWidth * INFO_PANEL_WINDOW_PORTION);
		currentInfoPanelSize = defaultInfoPanelSize;

		int header_height;
		if( isIframed() && ! isFullscreen() )
			header_height = 0;
		else
			header_height = HEADER_HEIGHT_PIXELS;

		layoutPanel.addNorth(header, header_height);
		layoutPanel.addSouth(footer, FOOTER_HEIGHT_PIXELS);
		layoutPanel.addWest(infoPanel, currentInfoPanelSize);
		layoutPanel.add(mapPanel);

	}

	private void layoutAsMobile() {
		// 40%
		if( responsiveMode.equals("mobile_portrait") ) {
		    defaultInfoPanelSize = (int) (windowHeight * INFO_PANEL_WINDOW_PORTION);
			currentInfoPanelSize = defaultInfoPanelSize;
			layoutPanel.addSouth(infoPanel, currentInfoPanelSize);
		} else {
			// landscape
		    defaultInfoPanelSize = (int) (windowWidth * INFO_PANEL_WINDOW_PORTION);
			currentInfoPanelSize = defaultInfoPanelSize;
			layoutPanel.addWest(infoPanel, currentInfoPanelSize);
		}
		layoutPanel.add(mapPanel);
	}

	private void setupWindowVariables() {

//	    // mobile devices shouldn't trigger a resize when the on-screen keyboard
//	    // appears. Detect device rotate by checking _both_ width and height
//	    // change.
//	    int newWidth = Window.getClientWidth();
//	    int newHeight = Window.getClientHeight();
//
//	    // check current responsive mode. This could be updated below.
//	    if( responsiveMode.startsWith("mobile") ) {
//    	    if( newWidth != windowWidth && newHeight != windowHeight ) {
//    	        windowWidth = newWidth;
//                windowHeight = newHeight;
//    	    }
//	    } else {
//	        windowWidth = newWidth;
//            windowHeight = newHeight;
//	    }

	    windowWidth = Window.getClientWidth();
	    windowHeight = Window.getClientHeight();
		responsiveMode = responsiveMode();
	}


	/**
	 * claim root panel and attach basic layout
	 */
	public void initialBuild() {
		setupWindowVariables();
		build();

		// reset mode so onResize() can correctly build the initial
		// page in a way consistent with a move between responsive
		// modes.
		responsiveMode = "unknown";

		// any HTML parsing here.

        // build UI
		infoPanelContent.onResize();

	}

	/**
	 * Called when responsive mode changes and when initial
	 * layout is created.
	 *
	 * On change in responsive mode it re-creates the layout panel.
	 */
	private void build() {

		if(layoutPanel != null)
			layoutPanel.removeFromParent();

		rootPanel.clear();

		layoutPanel = new DockLayoutPanel(Unit.PX);
		if( responsiveMode.equals("full_version") )
			 layoutAsDesktop();
		else layoutAsMobile();

	    rootPanel.add(layoutPanel);

	}

	/**
	 * to be called after window resizes and when layout is ready to display.
	 *
	 * It also hides/reveals responsive parts of the layout.
	 *
	 */
	public void onResize() {

	    if( resizeLocked ) {
	        logger.fine("Resize is locked");
	        return;
	    }

		String lastResponsiveMode = responsiveMode;
		setupWindowVariables();

		logger.fine("onResize called: window is "+windowWidth+"x"+windowHeight);
	    rootPanel.onResize();


		if( ! lastResponsiveMode.equals(responsiveMode) ) {

            // responsive mode has changed so re-create layout
            String msg = "Switching responsive mode from " + lastResponsiveMode;
            msg += " to " + responsiveMode;
            logger.fine(msg);

            build();
            setupControls();
            infoPanelContent.setResponsiveMode(responsiveMode);
            updateResponsiveElements();

            // CSS
            infoContent.removeStyleName("responsive_"+lastResponsiveMode);
            infoContent.addStyleName("responsive_"+responsiveMode);

            // the layout needs to have been drawn by the browser before
            // any of the responsive elements can find their sizes.
            final ResponsivePlusLayout me = this;
            Timer resizeTimer = new Timer() {
                @Override
                public void run() {
                    me.onResize();
                    if( responsiveMode.startsWith("mobile") ) {
                        // this should make mobile devices hide the url bar
                        logger.finer("scrolling to 0");
                        Window.scrollTo(0, 1);
                    }

                }
            };
            resizeTimer.schedule(100);

		}

        updateFolderTabs();
        infoPanelContent.onResize();
        if( mapReady )
            map.triggerResize();

	}

	private void updateResponsiveElements() {
       for( ResponsiveElement re : responsiveElements ) {

            Element el = Document.get().getElementById(re.target_element_id);
            if( el == null )
                continue;

            if( responsiveMode.equals(re.responsive_mode) ) {
                if( re.remove_class != null )
                    el.removeClassName(re.remove_class);

                if( re.add_class != null )
                    el.addClassName(re.add_class);
            } else {
                if( re.remove_class != null )
                    el.addClassName(re.remove_class);

                if( re.add_class != null )
                    el.removeClassName(re.add_class);
            }
        }
	}


	private void setupFolderTabs() {

	    if( folderTab != null ) {
	        folderTab.clear();
	        folderTab.removeFromParent();
	    }

	    folderTab = new FlowPanel();
	    Image tabImage;

	    if (responsiveMode.equals("full_version")) {

    	    tabImage = new Image(images.tab());
    	    tabImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    openPanel();
                }
            });
            folderTab.setVisible(false);
            folderTab.setStyleName("folder_tab");
            folderTab.add(tabImage);

	    } else {
	        // mobile modes

	        AbsolutePanel abPanel = new AbsolutePanel();

	        if(responsiveMode.equals("mobile_portrait")) {
    	        tabImage = new Image(images.tab_horizontal());
	            abPanel.setPixelSize(tabImage.getWidth(), tabImage.getHeight());
    	        abPanel.add(tabImage, 0, 0);

    	        expandOpen = new Image(images.expand_arrow_horizontal_out());
    	        expandOpen.setVisible(false);
    	        abPanel.add(expandOpen, 20, 5);

    	        expandClose = new Image(images.expand_arrow_horizontal_in());
    	        expandClose.setVisible(false);
                abPanel.add(expandClose, 70, 5);

    	        folderTab.setStyleName("folder_tab_horizontal");
	        } else { // if(responsiveMode.equals("mobile_landscape")) {
    	        tabImage = new Image(images.tab_vertical());
    	        abPanel.setPixelSize(tabImage.getWidth(), tabImage.getHeight());
                abPanel.add(tabImage, 0, 0);

    	        expandOpen = new Image(images.expand_arrow_vertical_out());
                expandOpen.setVisible(false);
                abPanel.add(expandOpen, 5, 20);

                expandClose = new Image(images.expand_arrow_vertical_in());
                expandClose.setVisible(false);
                abPanel.add(expandClose, 5, 70);

                folderTab.setStyleName("folder_tab_vertical");
	        }

	        expandOpen.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    openPanel();
                }
            });
	        expandClose.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    closePanel();
                }
            });

            tabImage.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showHideExpandArrows(true);
                }
            });

            folderTab.setVisible(true);
            folderTab.add(abPanel);
	    }
        mapPanel.add(folderTab);
	}

	private void updateFolderTabs() {

	    if (responsiveMode.equals("full_version")) {
	        int panelWidth = infoPanel.getOffsetWidth();
            if( panelWidth < 22 )
                folderTab.setVisible(true);
            else
                folderTab.setVisible(false);
	    } else {
	        showHideExpandArrows(true);
	    }
	}

    private void setupInfoPanelControls() {

        iconControls = new HorizontalPanel();
        iconControls.setStyleName("info_panel_controls");
        infoContent.insert(iconControls, 0);

        // general layout setup
        Image shrink = new Image(images.leftArrow());
        shrink.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                if( (currentInfoPanelSize-PANEL_RESIZE_PIXELS) < PANEL_RESIZE_PIXELS+50 )
                    closePanel();
                else {
                    currentInfoPanelSize -= PANEL_RESIZE_PIXELS;
                    resizeInfoPanel();
                }
            }
        });
        iconControls.add(shrink);

        Image enlarge = new Image(images.rightArrow());
        enlarge.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentInfoPanelSize += PANEL_RESIZE_PIXELS;
                resizeInfoPanel();
            }
        });
        iconControls.add(enlarge);

        Image close = new Image(images.cross());
        close.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                closePanel();
            }
        });
        iconControls.add(close);

    }

	/**
	 * this is called by initialBuild() and when the responsive mode
	 * changes. It adds controls to change the size of the info panel.
	 */
    private void setupControls() {
        if (responsiveMode.equals("full_version")) {
            setupInfoPanelControls();
            iconControls.setVisible(true);
        } else if (responsiveMode.startsWith("mobile_") ) {
            if( iconControls != null ) {
                iconControls.setVisible(false);
            }


        }
        setupFolderTabs();
    }

    public String responsiveMode() {
		String r;
		if( isMobile() ) {
			if( windowHeight > windowWidth )
				r = "mobile_portrait";
			else
				r = "mobile_landscape";
		} else
			r = "full_version";
		return r;
	}

	public boolean isMobile() {
		return windowWidth <= MOBILE_THRESHOLD_PIXELS
			|| windowHeight <= MOBILE_THRESHOLD_PIXELS;
	}

	public boolean isIframed() {
		String baseUrl = getParentUrl();
	    String ourUrl = Window.Location.getHref();
	    return ! baseUrl.equals(ourUrl);
	}

	public Widget getInfoPanel() {
		return (Widget) infoContent;
	}

//	public CarouselBasedInfoPanel getInfoPanelContent() {
//	    return infoPanelContent;
//	}

	/**
	 * browser has full screen mode - not tested
	 * @return
	 */
	public native boolean hasFullscreen() /*-{
		var fullscreenEnabled = $doc.fullscreenEnabled
				|| $doc.mozFullScreenEnabled || $doc.webkitFullscreenEnabled;
		return fullscreenEnabled;
    }-*/;

	public native boolean isFullscreen() /*-{
		var fullscreenElement;
		var fullscreenEnabled = $doc.fullscreenEnabled
				|| $doc.mozFullScreenEnabled || $doc.webkitFullscreenEnabled;

		if (!fullscreenEnabled)
			return false;

		if ($doc.fullscreenEnabled)
			fullscreenElement = $doc.fullscreenElement;
		else if ($doc.mozFullScreenEnabled)
			fullscreenElement = $doc.mozFullScreenElement;
		else if ($doc.webkitFullscreenEnabled)
			fullscreenElement = $doc.webkitFullscreenElement;

		return fullscreenElement != null;
    }-*/;

	/**
	 *
	 * @return url of parent frame. This will only work (i.e. security exception)
	 * when the site fully occupies the browser or is an iframe from the same
	 * domain as the parent.
	 */
    private static final native String getParentUrl() /*-{
		try {
			return $wnd.parent.location.href;
		} catch (e) {
			return "";
		}
    }-*/;

}
