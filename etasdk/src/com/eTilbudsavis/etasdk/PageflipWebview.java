/**
 * @fileoverview	Pageflip.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

import com.eTilbudsavis.etasdk.EtaLocation.LocationListener;
import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonStringRequest;
import com.eTilbudsavis.etasdk.NetworkHelpers.StringRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * PageflipWebview class documentation is to be extended
 * 
 * <br><br>
 * 
 * There are a couple of definitions to keep in mind when reading documentation about
 * {@link #com.eTilbudsavis.etasdk.PageflipWebview PageflipWebview}.
 * <li>PageflipWebview is defined as this object, which a developer should interact with.</li>
 * <li>Pageflip is defined as the JavaScript running inside the PageflipWebview.</li>
 * 
 * <br><br>
 * For further information, please refer to the Pageflip documentation found in the
 * <a href="http://engineering.etilbudsavis.dk/eta-javascript-sdk/">JavaScript Documentation</a>
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public final class PageflipWebview extends WebView {
	
	private static final String TAG = "PageflipWebview";

	/**
	 * Variable controlling debugging mode for PageflipWebview. If set to true, a WebChromeClient will be enabled.
	 */
	public static boolean DEBUG = false;
	
	private static final String PAGEFLIP_PROXY_NAME = "AndroidInterface";
	
	/**
	 * Various standard events that the Pageflip JavaScript dispaches via
	 * {@link #com.eTilbudsavis.etasdk.PageflipWebview.PageflipJavaScriptInterface PageflipJavaScriptInterface}.
	 * <br><br>
	 * For further information, please refer to the Pageflip documentation found in the
	 * <a href="http://engineering.etilbudsavis.dk/eta-javascript-sdk/">JavaScript Documentation</a>
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Event {
		
		/** Event identifying that Pageflip JavaScript is ready for user interaction */
		private static final String PROXY_READY = "eta-proxy-ready";
		
		/** Event identifying session changes */
		private static final String SESSION_CHANGE = "eta-session-change";

		/** Event identifying Pageflip requesting data from the API */
		private static final String API_REQUEST = "eta-api-request";

		/** Event identifying a page change in Pageflip */
		public static final String PAGECHANGE = "eta-catalog-view-pagechange";

		/** Event identifying that pageflip is out-of-bounds */
		public static final String OUTOFBOUNDS = "eta-catalog-view-outofbounds";
		
		/** Event identifying that a hotspot have been clicked in Pageflip */
		public static final String HOTSPOT = "eta-catalog-view-hotspot";

		/** Event identifying a single tap in Pageflip */
		public static final String SINGLETAP = "eta-catalog-view-singletap";

		/** Event identifying a double tap in Pageflip */
		public static final String DOUBLETAP = "eta-catalog-view-doubletap";

		/** Event identifying dragging in Pageflip */
		public static final String DRAGSTART = "eta-catalog-view-dragstart";

		/** Event identifying an error in Pageflip */
		public static final String ERROR = "eta-catalog-view-error";
		
	}

	/**
	 * Various standard variables used as options to feed the Pageflip JavaScript
	 * <br><br>
	 * For further information, please refer to the Pageflip documentation found in the
	 * <a href="http://engineering.etilbudsavis.dk/eta-javascript-sdk/">JavaScript Documentation</a>
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Option {

		/** Identifier for catalog id to load into Pageflip */
		public static final String CATALOG = "catalog";
		
		/** Identifier for the initial page to load */
		public static final String PAGE = "page";
		
		/** Identifier for enabling/disabling hotspots */
		public static final String HOTSPOTS = "hotspots";
		
		/** Identifier for enabling/disabling the hotspots overlay */
		public static final String HOTSPOT_OVERLAY = "hotspotOverlay";
		
		/** String identifying the option to have a close option in Pageflip */
		public static final String CAN_CLOSE = "canClose";
		
		/** String identifying the option to enabling/disabling the header */
		public static final String HEADLESS = "headless";
		
		/** String identifier for the put of bounds option*/
		public static final String OUT_OF_BOUNDS = "outOfBounds";
		
		/** String identifying the white lable (no eTilbudsavis branding) option. <b>Using this option may violate the terms of use.</b> */
		public static final String WHITE_LABLE = "whiteLabel";
		
		/** String identifier for session changes */
		public static final String SESSION_CHANGE = "session-change";
		
		/** Identifier for enabling overide of API requests. You shouldn't be using this unless you know what you are doing. */
		public static final String OVERRIDE_API = "overrideAPI";
		
	}
	
	/**
	 * Various variables used to inititlize Pageflip JavaScript.
	 * <br><br>
	 * For further information, please refer to the Pageflip documentation found in the
	 * <a href="http://engineering.etilbudsavis.dk/eta-javascript-sdk/">JavaScript Documentation</a>
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Init {

		public static final String API_KEY = "apiKey";
		public static final String API_SECRET = "apiSecret";
		public static final String SESSION = "session";
		// TODO: Check validity of this variable, is this still used?
		public static final String TOKEN_TTL = "tokenTTL";
		public static final String LOCALE = "locale";
		public static final String LOCATION = "geolocation";
		public static final String LOCATION_LAT = "latitude";
		public static final String LOCATION_LNG = "longitude";
		public static final String LOCATION_SENSOR = "sensor";
		public static final String LOCATION_RADIUS = "radius";
		public static final String USERAGENT = "userAgent";
		
	}

	/**
	 * Various commands that may be used for controlling Pageflip JavaScript
	 * <br><br>
	 * For further information, please refer to the Pageflip documentation found in the
	 * <a href="http://engineering.etilbudsavis.dk/eta-javascript-sdk/">JavaScript Documentation</a>
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Command {
		
		/** Command to initialize Pageflip, see {@link #com.eTilbudsavis.etasdk.PageflipWebview.Init Init} for available options */
		public static final String INITIALIZE = "initialize";
		
		/** Command for sending configurations */
		public static final String CONFIGURE = "configure";
		
		/** String identifying the option to toggle visibility of the thumbnail view, in pageflip. @see {@link #toggleThumbnails() toggleThumbnails()}*/
		public static final String CATALOG_VIEW_THUMBNAILS = "catalog-view-thumbnails";

		/** String identifying the option to go to a certain page in pageflip @see {@link #gotoPage(int) gotoPage()}*/
		public static final String CATALOG_VIEW_GOTO_PAGE = "catalog-view-go-to-page";

		/** String identifying the option to close the JavaScript inside PageflipWebview */
		public static final String CATALOG_VIEW_CLOSE = "catalog-view-close";
		
		/** Command for sending location changes to Pageflip JS */
		public static final String GEOLOCATION_CHANGE = "geolocation-change";
		
		/** String identifying the catalog-view parameter, used when loading a catalog. All available options for this parameter is prefixed with "OPTION" as in {@link PageflipWebview#OPTION_CATALOG OPTION_CATALOG} */
		public static final String CATALOG_VIEW = "catalog-view";
		
		/** Command for propagating completed api requests back to Pageflip JS */
		public static final String API_REQUEST_COMPLETE = "api-request-complete";
		
	}
	
	/**
	 * The {@link #com.eTilbdusavis.etasdk.Eta Eta} object to interact with
	 */
	private Eta mEta;
	
	/**
	 * A randomly generated UUID to use for the Pageflip HTML
	 */
	private String mUuid;
	
	/**
	 * {@link #com.eTilbdusavis.etasdk.PageflipWebview.PageflipListener Pageflip listener} for callbacks
	 */
	private PageflipListener mListener;
	
	/** 
	 * The id of the catalog to display in the PageflipWebview
	 */
	private String mCatalogId;
	
	/** 
	 * Options to inject into Pageflip on init
	 */
	private JSONObject mCatalogViewOptions = new JSONObject();
	
	/**
	 * Variable keeping state of whether thumbs are currently being displayed in the pageflip
	 */
	private boolean mThumbnailsToggled = false;
	
	/**
	 * State determining whether Pageflip JavaScript, is ready for user interaction
	 */
	private boolean mReady = false;
	
	/** 
	 * The interface between PageflipWebview And Pageflip
	 */
	private PageflipJavaScriptInterface mPFInterface;
	
	/** 
	 * To avoid recursion, when Pageflip updates session
	 */
	private boolean mSessionFromPageflip = false;
	
	/**
	 * List of all active Pageflip's. 
	 * SDK uses this list to propagate session- and location changes into the Pageflip's
	 */
	public static List<PageflipWebview> pageflips = new ArrayList<PageflipWebview>();
	
	/**
	 * The default HTML listener, for receiving Pageflip HTML base.
	 */
	private Listener<String> htmlListener = new Listener<String>() {

		public void onComplete(String response, EtaError error) {
			
			if (response == null) {
				loadDataWithBaseURL(null, "<html><body>" + error.toString() + "</body></html>", "text/html", "utf-8", null);
				mListener.onEvent(Event.ERROR, mUuid, error.toJSON());
			} else {
				loadDataWithBaseURL(null, response, "text/html", "utf-8", null);
			}
			
		}
	};
	
	/**
	 * Construct a new PageflipWebview from Context object
	 * @param context
	 */
	public PageflipWebview(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Construct a new PageflipWebview with layout parameters.
	 * @param context A Context object used to access application assets.
	 * @param attrs An AttributeSet passed to our parent.
	 */
	public PageflipWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
		init();
    }
	
	/**
	 * Construct a new WebView with layout parameters and a default style.
	 * @param context A Context object used to access application assets.
	 * @param attrs An AttributeSet passed to our parent.
	 * @param defStyle The default style resource ID.
	 */
	public PageflipWebview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	/**
	 * Initializing of default values, e.t.c. when constructing this PageflipWebview
	 */
	private void init() {
        setHeadless(true);
        setOutOfBounds(false);
	}
	
	/**
	 * WebChromeClient is mainly for debugging the WebView.
	 */
	WebChromeClient wcc = new WebChromeClient() {
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
			EtaLog.d(TAG, "JsAlert: " + message);
			if (!Eta.getInstance().isResumed())
				return true;
			
			new AlertDialog.Builder(mEta.getContext())  
            .setTitle("JavaScript Alert")  
            .setMessage(message)  
            .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() { 
            	public void onClick(DialogInterface dialog, int which) { result.confirm(); } })
            .setCancelable(false)
            .create()
            .show();
        return true;  
		}
	};
	
	/**
	 * Execute, will begin the initialization of the pageflip.
	 * And load the specified catalog.
	 * @param eta containing relevant API-key e.t.c.
	 * @param Listener for pageflip events
	 * @param CatalogId of the catalog to display
	 */
	@SuppressLint("NewApi")
	public void execute(Eta eta, PageflipListener Listener, String CatalogId) {
		
		// Instantiate variables for PageflibWebview
		mEta = eta;
		mListener = Listener;
		mCatalogId = CatalogId;
		mUuid = Utils.createUUID();
		mPFInterface = new PageflipJavaScriptInterface();
		pageflips.add(this);
		
		// Create settings for Webview to function properly
		WebSettings ws = getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setDefaultTextEncodingName("utf-8");
		ws.setRenderPriority(RenderPriority.HIGH);
		ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		ws.setAllowFileAccess(true);
		addJavascriptInterface(mPFInterface, PAGEFLIP_PROXY_NAME);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
		// Add debugging if needed
		if (DEBUG) {	
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			    WebView.setWebContentsDebuggingEnabled(true);
			}
			setWebChromeClient(wcc);
		}
		
		// Get the HTML, and get Pageflip running
		String url = Endpoint.pageflipProxy(mUuid);
//		url = "http://10.0.1.41:3000/proxy/%s/";
		StringRequest req = new StringRequest(url, htmlListener);
		mEta.add(req);
		
	}
	
	private JSONObject location() {
		JSONObject loc = new JSONObject();
		try {
			loc.put(Init.LOCATION_LAT, mEta.getLocation().getLatitude());
			loc.put(Init.LOCATION_LNG, mEta.getLocation().getLongitude());
			loc.put(Init.LOCATION_SENSOR, mEta.getLocation().isSensor());
			loc.put(Init.LOCATION_RADIUS, mEta.getLocation().getRadius());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return loc;
	}
	
	/**
	 * Method to initialize the Javascript in the pageflip, via the etaProxy.<br>
	 * Method for loading a catalog, into the initialized pageflip.<br>
	 * This method should not be called, before eta-proxy:ready has been triggered.<br>
	 */
	private void initializeJavaScript() {
		
		try {
			// Inject global configuration
			JSONObject config = new JSONObject();
			config.put(Option.OVERRIDE_API, true);
			mPFInterface.etaProxy(Command.CONFIGURE, config);
			
			// Inject catalog, and device specific info
			JSONObject init = new JSONObject();
			init.put(Init.API_KEY, mEta.getApiKey());
			init.put(Init.API_SECRET, mEta.getApiSecret());
			JSONObject ses = mEta.getSessionManager().getSession().toJSON();
			init.put(Init.SESSION, ses);
			init.put(Init.LOCALE, Locale.getDefault().toString());
			init.put(Init.USERAGENT, mEta.getAppVersion());
			if (mEta.getLocation().isSet()) {
				init.put(Init.LOCATION, location());
			}
			mPFInterface.etaProxy(Command.INITIALIZE, init);
			
			// Inject view options
			setOption(Option.CATALOG, mCatalogId);
			mPFInterface.etaProxy(Command.CATALOG_VIEW, mCatalogViewOptions);
			// Set catalog view options to null to prevent further use of this object.
			mCatalogViewOptions = null;
		
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
	}

	private LocationListener ll = new LocationListener() {
		
		public void onLocationChange() {
			mPFInterface.etaProxy(Command.GEOLOCATION_CHANGE, location());
		}
		
	};
	
	@SuppressLint("NewApi")
	public void onPause() {
		mPFInterface.etaProxy("pause");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			super.onPause();
		}
		
	}
	
	@SuppressLint("NewApi")
	public void onResume() {
		mEta.getLocation().subscribe(ll);
		mPFInterface.etaProxy("resume");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			super.onResume();
		}
		
	}
	
	public boolean isThumbnailsToggled() {
		return mThumbnailsToggled;
	}
	
	/**
	 * Interface for showing/hiding the thumbnail list.
	 */
	public void toggleThumbnails() {
		mThumbnailsToggled = !mThumbnailsToggled;
		mPFInterface.etaProxy(Command.CATALOG_VIEW_THUMBNAILS);
	}
	
	public void updateSession() {
		if (!mSessionFromPageflip) {
			Session s = mEta.getSessionManager().getSession();		
			mPFInterface.etaProxy(Option.SESSION_CHANGE, s.toJSON());
		}
		mSessionFromPageflip = false;
	}
	
	public void closePageflip() {
		if (mPFInterface != null) {
			Eta.getInstance().getLocation().unSubscribe(ll);
			pageflips.remove(PageflipWebview.this);
			mPFInterface.etaProxy(Command.CATALOG_VIEW_CLOSE);
		}
	}
	
	/**
	 * Method for going to a specific page AFTER pageflip have been initialized.
	 * @param page to go to
	 */
	public void gotoPage(int page) {
		try {
			JSONObject data = new JSONObject();
			data.put("page", String.valueOf(page));
			data.put("animated", "false");
			mPFInterface.etaProxy(Command.CATALOG_VIEW_GOTO_PAGE, data);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
	}

	/**
	 * Method for determining if the Pageflip is ready.
	 * @return true if Pageflip JavaScript is ready for user-input
	 */
	public boolean isReady() {
		return mReady;
	}
	
	/**
	 * The desired page to start on.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param page number
	 */
	public void setPage(int page) {
		setOption(Option.PAGE, String.valueOf(page));
	}
	
	/**
	 * Whether to enable or disable hotspots.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param True to enable, else false
	 */
	public void setHotspotsEnabled(boolean enabled) {
		setOption(Option.HOTSPOTS, String.valueOf(enabled));
	}
	
	/**
	 * Whether to disable hotspot overlay or not.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param enabled or not
	 */
	public void setHotspotOverlayVisible(boolean visible) {
		setOption(Option.HOTSPOT_OVERLAY, String.valueOf(visible));
	}

	/**
	 * Set whether the catalog view can close or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param closable or not
	 */
	public void setCanClose(boolean closeable) {
		setOption(Option.CAN_CLOSE, String.valueOf(closeable));
	}

	/**
	 * Whether the header should be disabled or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param useHeader or not
	 */
	private void setHeadless(boolean headless) {
		setOption(Option.HEADLESS, String.valueOf(headless));
	}

	/**
	 * Whether to show out of bounds dialog or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param display or not
	 */
	private void setOutOfBounds(boolean displayOutOfBounds) {
		setOption(Option.OUT_OF_BOUNDS, String.valueOf(displayOutOfBounds));
	}

	/**
	 * Whether to disable branding or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param display or not
	 */
	public void setWhiteLable(boolean displayBranding) {
		setOption(Option.WHITE_LABLE, String.valueOf(displayBranding));
	}
	
	/**
	 * Generic method for setting pageflip options.<br>
	 * If an option isn't available through any other pageflip method,
	 * you can use this method for setting options in the pageflip.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param key the option to set
	 * @param value the value of the option
	 */
	public void setOption(String key, String value) {
		if (mCatalogViewOptions == null) {
			EtaLog.printStackTrace();
			EtaLog.d(TAG, "Calling setOption() efter invoking execute() isn't allowed");
		} else {
			try {
				mCatalogViewOptions.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Helper class for communication between PageflipWebview and the JavaScript
	 * residing inside the Webview. This class must be used for communication in order to
	 * avoid errors in URLEncoding, as the standard way of communication between the two
	 * generates errors when injecting data like e.g. hotspots.
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 *
	 */
	public class PageflipJavaScriptInterface {
		
		/** A list of responses from the eTilbudsavis API v2, that pageflip have at some point in time requested. */
		HashMap<String, JSONObject> mResponses = new HashMap<String, JSONObject>();
		
		/**
		 * Interface for receiving new events from Pageflip.
		 * Pageflip dispatches new event data via this method, and sends a new event which
		 * must be a string with JSONObject-syntax. The JSOBObject must
		 * also contain two specific keys:
		 * <li> eventName - a String describing a specific event.</li>
		 * <li> data - the actual payload that is to be handled</li>
		 * 
		 * Some events that are dispatched via this method, is to be handled by the
		 * SDK, others should just be propagated on to the user.
		 * 
		 * @param payload the String 
		 */
		@JavascriptInterface
		public void dispatch(String payload) {
			
			try {
				
				JSONObject tmp = new JSONObject(payload);
				final String event = tmp.getString("eventName");
				final JSONObject data = tmp.getJSONObject("data");
				
				// Only events for users must go to UI-thread. SDK events are handled in another manor.
				if (event.equals(Event.SESSION_CHANGE) || event.equals(Event.PROXY_READY) || event.equals(Event.API_REQUEST)) {
					onEvent(event, data);
				} else {
					mEta.getHandler().post(new Runnable() {
						
						public void run() {
							onEvent(event, data);
						}
					});
				}
				
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			
		}
		
		/**
		 * Interface that Pageflip JavaScript must use for retrieving data that have previously been
		 * requested via {@link #dispatch(String) dispatch()}, this for now only applies for events
		 * of the type "eta-api-request", where data is actually being requested.
		 * @param id of the data pageflip wants.
		 * @return a string of JSONObject data
		 */
		@JavascriptInterface
		public String getData(String id) {
			JSONObject resp = mResponses.get(id);
			String r = "";
			if (resp != null) {
				mResponses.remove(id);
				r = resp.toString();
			}
			return r;
		}
		
		private void etaProxy(String command, JSONObject data) {
			etaProxy(Utils.createUUID(), command, data);
		}
		
		/**
		 * Wrapper for the "window.etaProxy.push" command.
		 * @param command to push in pageflip
		 * @param data to handle in pageflip
		 */
		private void etaProxy(String id, String command, JSONObject data) {
			
			mResponses.put(id, data);
			JSONObject o = new JSONObject();
			try {
				o.put("id", id);
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			
			injectJS(String.format("window.etaProxy.push( ['%s', '%s'] );", command, o.toString()));
			
		}
		
		
		
		/**
		 * Wrapper for the "window.etaProxy.push" command.
		 * @param command to push in pageflip
		 */
		private void etaProxy(String command) {
			injectJS(String.format("window.etaProxy.push( ['%s'] );", command));
		}
		
		/**
		 * Wrapper for JavaScript commands to be injected into the pageflip.
		 * @param option a snippet of JavaScript
		 */
		private void injectJS(String jsCommand) {
			
			if (isReady()) {
				EtaLog.d(TAG, jsCommand);
			}
			
			final String s = String.format("javascript:(function() { %s })()", jsCommand);
			
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					loadUrl(s);
				}
			});
		}
		
		public void onEvent(String event, JSONObject data){
			
			if (event.equals(Event.SESSION_CHANGE)) {
				
				mSessionFromPageflip = true;
				mEta.getSessionManager().setSession(data);
				
			} else if (event.equals(Event.PROXY_READY)) {
				
				initializeJavaScript();
				
			} else if (event.equals(Event.API_REQUEST)) {
				
				try {
					
					JSONObject request = data.getJSONObject("data");
					
					final String rId = data.getString("id");
					final String rUrl = request.getString("url");
					final String rBody = request.has("data") ? request.getString("data") : null;
					
					int rMethod = Method.GET;
					if (request.has("type")) {
						String m = request.getString("type").toLowerCase();
						if (m.equals("put")) {
							rMethod = Method.PUT;
						} else if (m.equals("delete")) {
							rMethod = Method.DELETE;
						} else if (m.equals("post")) {
							rMethod = Method.POST;
						}
					}
					
					Map<String, String> rHeaders = new HashMap<String, String>();
					if (request.has("headers")) {
						JSONObject h = request.getJSONObject("headers");
						Iterator<?> keys = h.keys();
				        while( keys.hasNext() ){
				            String key = (String)keys.next();
				            rHeaders.put(key, h.getString(key));
				        }
					}
					
					// The request, to be performed
					JsonStringRequest req = new JsonStringRequest(rMethod, rUrl, rBody, new Listener<String>() {
						
						public void onComplete(String response, EtaError error) {

							try {
								
								JSONObject resp = new JSONObject();
								resp.put("id", rId);
								
								// Handcraft JSON, as we don't know if it's a JSONArray or JSONObject
								if (response != null) {
									if (response.startsWith("[") && response.endsWith("]")) {
										resp.put("success", new JSONArray(response));
									} else if (response.startsWith("{") && response.endsWith("}")) {
										resp.put("success", new JSONObject(response));
									}
								} else {
									resp.put("error", error.toJSON());
								}
								
								etaProxy(rId, Command.API_REQUEST_COMPLETE, resp);
								
							} catch (JSONException e) {
								EtaLog.d(TAG, e);
							}
							
						}
					});
//					req.debugNetwork(true);
					mEta.add(req);
					
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
				
			} else {
				
				if (event.equals(Event.PAGECHANGE)) {
					
					try {
						if (data.has("init") && data.getBoolean("init")) {
							mReady = true;
							mListener.onReady(mUuid);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}
				
				// Send standard event
				mListener.onEvent(event, mUuid, data);
				
			} 
			
		}
		
	}
	
	/**
	 * Callback interface for Pageflip.
	 * Used for callback's on events in the WebView
	 */
	public interface PageflipListener {
		
		/**
		 * Called when a Pageflip event happens.<br>
		 * There are several events that may occur, but this interface only propagates the events
		 * that might be relevant for anyone implementing the PageflipWebview
		 * 
		 * @param event The type of event
		 * @param uuid
		 * @param object The data received from pageflip
		 */
		public void onEvent(String event, String uuid, JSONObject object);
		
		/**
		 * A method call indicating that the JavaScript inside PageflipWebview is ready.
		 * From this point on, users may start interacting with the PageflipWebview.
		 * @param uuid of the calling PageflipWebview
		 */
		public void onReady(String uuid);
	}
	
}