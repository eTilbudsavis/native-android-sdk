/**
 * @fileoverview	Pageflip.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.eTilbudsavis.etasdk;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eTilbudsavis.etasdk.EtaLocation.LocationListener;
import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache.Item;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressLint("SetJavaScriptEnabled")
public final class Pageflip extends WebView {

	private static final String TAG = "Pageflip";

	/** String identifying etaProxy events */
	private static final String ETA_PROXY = "eta-proxy";
	/** String identifying the ready pageflip event. See pageflip documentation for more details */
	public static final String EVENT_READY = "ready";

	/** String identifying session events */	
	private static final String ETA_SESSION = "eta-session";
	/** String identifying the change event, called when the session has changed. See pageflip documentation for more details */
	public static final String EVENT_CHANGE = "change";
	
	/** String identifying session events */
	private static final String ETA_THUMB = "catalog-view-thumbnails";
	
	/** String identifying the initialize parameter, used when initializing pageflip */
	private static final String PARAM_INITIALIZE = "initialize";
	private static final String API_KEY = "apiKey";
	private static final String API_SECRET = "apiSecret";
	private static final String SESSION = "session";
	private static final String LOCALE = "locale";
	private static final String LOCATION = "geolocation";
	private static final String LOCATION_LAT = "latitude";
	private static final String LOCATION_LNG = "longitude";
	private static final String LOCATION_SENSOR = "sensor";
	private static final String LOCATION_RADIUS = "radius";



	/** String identifying the catalog-view parameter, used when loading a catalog. All available options for this parameter is prefixed with "OPTION" as in {@link Pageflip#OPTION_CATALOG OPTION_CATALOG} */
	private static final String PARAM_CATALOG_VIEW = "catalog-view";
	/** String identifying the catalog option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
//	private static final String OPTION_VIEW_SESSION = "viewSession";
	private static final String OPTION_VIEW_SESSION = "viewSession";
	/** String identifying the catalog option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_CATALOG = "catalog";
	/** String identifying the page option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_PAGE = "page";
	/** String identifying the hotspots option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HOTSPOTS = "hotspots";
	/** String identifying the hotspot overlay option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HOTSPOT_OVERLAY = "hotspotOverlay";
	/** String identifying the can close option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_CAN_CLOSE = "canClose";
	/** String identifying the headless option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HEADLESS = "headless";
	/** String identifying the put of bounds option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_OUT_OF_BOUNDS = "outOfBounds";
	/** String identifying the white lable option, used when setting options with the parameter {@link Pageflip#PARAM_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details<br>
	 * <b>NOTE</b> The usage of this option can violate the terms of use. */
	private static final String OPTION_WHITE_LABLE = "whiteLabel";
	

	/** String identifying the prefix for a view event. All available events this that follow this event is prefixed with "EVENT" as in {@link Pageflip#EVENT_PAGECHANGE EVENT_PAGECHANGE} */
	public static final String ETA_CATALOG_VIEW = "eta-catalog-view";
	/** String identifying the pagechange pageflip event. See pageflip documentation for more details */
	public static final String EVENT_PAGECHANGE = "pagechange";
	/** String identifying the outofbounds pageflip event. See pageflip documentation for more details */
	public static final String EVENT_OUTOFBOUNDS = "outofbounds";
	/** String identifying the hotspot pageflip event. See pageflip documentation for more details */
	public static final String EVENT_HOTSPOT = "hotspot";
	/** String identifying the singletap pageflip event. See pageflip documentation for more details */
	public static final String EVENT_SINGLETAP = "singletap";
	/** String identifying the doubletap pageflip event. See pageflip documentation for more details */
	public static final String EVENT_DOUBLETAP = "doubletap";
	/** String identifying the dragstart pageflip event. See pageflip documentation for more details */
	public static final String EVENT_DRAGSTART = "dragstart";
	
	private Eta mEta;
	private String mUuid;
	private PageflipListener mListener;
	private String mCatalogId;
	private JSONObject mCatalogView = new JSONObject();
	private String mDebugWeinre = null;
	private List<Pageflip> mPageflips;
	private String mViewSession = null;
	
	/**
	 * Used for manual inflation
	 * @param context
	 */
	public Pageflip(Context context) {
		super(context);
		setHeadless(true);
        setOutOfBounds(false);
	}
	
	/**
	 * Constructor used when inflating Pageflip from XML.
	 * @param context
	 * @param attrs
	 */
	public Pageflip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHeadless(true);
        setOutOfBounds(false);
    }

	/**
	 * The WebViewClient used in the WebView as a communication proxy.
	 */
	WebViewClient wvc = new WebViewClient() {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			
			String[] request = url.split(":", 3);
			
			if ( request.length < 2 )
				return false;
			
			JSONObject o = decodeData(request[2]);
			
			if (request[0].equals(ETA_CATALOG_VIEW)) {

				// Send ready callback
				if (request[1].equals(EVENT_PAGECHANGE) && o.has("init")) {
					try {
						if (o.getBoolean("init")) {
							mViewSession = o.getString(OPTION_VIEW_SESSION);
							mListener.onReady(mUuid, mViewSession);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				// Send standart event
				mListener.onEvent(request[1], mUuid, o);
				
			} else if (request[0].equals(ETA_SESSION)) {
				
				mEta.getSessionManager().setSession(Session.fromJSON(o));
				
			} else if (request[0].equals(ETA_PROXY)) {
				
				// On document ready, run catalog-view options
				if (request[1].equals(EVENT_READY)) {
					initializeJavaScript();
				}
			}
			return true;
			
		}

		// Notify when loading of WebView is done, now insert JavaScript init.
		public void onPageFinished(WebView view, String url) {
			
		}
	};
	
	private JSONObject decodeData(String data) {
		
		try {
			JSONObject o;
			
			if (data.length() == 0) {
				
				o = new JSONObject();
				
			} else {

				String resp = "{}";
				try {
					resp = URLDecoder.decode(data, "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				o = new JSONObject(resp);

				if (o.has("data"))
					o = o.getJSONObject("data");
				
			}
			return o;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return new JSONObject();
	}
	
	/**
	 * WebChromeClient is mainly for debugging the WebView.
	 */
	WebChromeClient wcc = new WebChromeClient() {
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
			Utils.logd(TAG, "JsAlert: " + message);
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
	@SuppressWarnings("unchecked")
	public void execute(Eta eta, PageflipListener Listener, String CatalogId) {

		mEta = eta;
		mListener = Listener;
		mCatalogId = CatalogId;
		mUuid = Utils.createUUID();

		try {
			Field f = mEta.getClass().getDeclaredField("mPageflips");
			f.setAccessible(true);
			mPageflips = (List<Pageflip>) f.get(mEta);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mPageflips != null && !mPageflips.contains(Pageflip.this)) {
			mPageflips.add(this);
		}
		
		getSettings().setJavaScriptEnabled(true);
		getSettings().setDefaultTextEncodingName("utf-8");
		setWebViewClient(wvc);
		
//		if (Eta.DEBUG) setWebChromeClient(wcc);
		
		// Check if it's necessary to update the HTML (it's time consuming to download HTML).
		String html = mEta.getSettings().getPageflipHtml(mUuid);
		
		if (html != null ) {
			
			this.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
			
		} else {
			
			StringListener cb = new StringListener() {
				
				public void onComplete(boolean isCache, int statusCode, String data, EtaError error) {
					
					if (mDebugWeinre != null) {
						String endScript = "</head>";
						data = data.replaceFirst(endScript, mDebugWeinre + endScript);
					}
					
					if (Utils.isSuccess(statusCode)) {
						
						mEta.getSettings().setPageflipHtml(mUuid, data);
						loadDataWithBaseURL(null, data, "text/html", "utf-8", null);
					} else {
						loadDataWithBaseURL(null, "<html><body>" + error.toString() + "</body></html>", "text/html", "utf-8", null);
					}
				}
			};
			mEta.getApi().get(Endpoint.getPageflipProxy(mUuid), cb).execute();
			
		}
		
	}
	
	/**
	 * Method to initialize the Javascript in the pageflip, via the etaProxy.<br>
	 * Method for loading a catalog, into the initialized pageflip.<br>
	 * This method should not be called, before eta-proxy:ready has been triggered.<br>
	 */
	private void initializeJavaScript() {
		
		// General setup of Pageflip JS
		JSONObject o = new JSONObject();
		try {
			o.put(API_KEY, mEta.getApiKey());
			o.put(API_SECRET, mEta.getApiSecret());
			o.put(SESSION, mEta.getSessionManager().getSession().toJSON());
			o.put(LOCALE, Locale.getDefault().toString());
			if (mEta.getLocation().isSet()) {
				JSONObject loc = new JSONObject();
				try {
					loc.put(LOCATION_LAT, mEta.getLocation().getLatitude());
					loc.put(LOCATION_LNG, mEta.getLocation().getLongitude());
					loc.put(LOCATION_SENSOR, mEta.getLocation().isSensor());
					loc.put(LOCATION_RADIUS, mEta.getLocation().getRadius());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				o.put(LOCATION, loc);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		etaProxy(PARAM_INITIALIZE, o);
		
		// Initialize the catalog
		try {

			mCatalogView.put(OPTION_CATALOG, mCatalogId);
			if (mViewSession != null)
				mCatalogView.put(OPTION_VIEW_SESSION, mViewSession);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		etaProxy(PARAM_CATALOG_VIEW, mCatalogView);
		
	}
	
	/**
	 * Wrapper for the "window.etaProxy.push" command.
	 * @param parameter
	 * @param data
	 */
	private void etaProxy(String parameter, JSONObject data) {
		etaProxy(parameter + "', '" + data.toString());
	}

	/**
	 * Wrapper for the "window.etaProxy.push" command.
	 * @param parameter
	 * @param data
	 */
	private void etaProxy(String parameter) {
		StringBuilder sb = new StringBuilder();
		sb.append("window.etaProxy.push(['")
		.append(parameter)
		.append("']);");
		injectJS(sb.toString());
	}
	
	/**
	 * Wrapper for JavaScript commands to be injected into the pageflip.
	 * @param option a snippet of JavaScript
	 */
	private void injectJS(String option) {
		String s = "javascript:(function() { " + option + "})()";
		loadUrl(s);
	}
	
	LocationListener ll = new LocationListener() {
		
		public void onLocationChange() {
			// TODO: Propagate change into pageflip
		}
	};
	
	
	public void onPause() {
		mEta.getLocation().unSubscribe(ll);
	}
	
	public void onResume() {
		mEta.getLocation().subscribe(ll);
	}
	
	public void useWeinreDebugger(String hostIp, String hostPort) {
		mDebugWeinre = "<script src=\"http://" + hostIp + ":" + hostPort + "/target/target-script-min.js\"></script>";
	}
	
	/**
	 * Interface for showing/hiding the thumbnail list.
	 */
	public void toggleThumbnails() {
		etaProxy(ETA_THUMB);
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
		try {
			mCatalogView.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The desired page to start on.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param page number
	 */
	public void setPage(int page) {
		try {
			mCatalogView.put(OPTION_PAGE, page);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Whether to enable or disable hotspots.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param True to enable, else false
	 */
	public void setHotspotsEnabled(boolean enabled) {
		try {
			mCatalogView.put(OPTION_HOTSPOTS, enabled);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether to disable hotspot overlay or not.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param enabled or not
	 */
	public void setHotspotOverlayVisible(boolean visible) {
		try {
			mCatalogView.put(OPTION_HOTSPOT_OVERLAY, visible);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set whether the catalog view can close or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param closable or not
	 */
	public void setCanClose(boolean closeable) {
		try {
			mCatalogView.put(OPTION_CAN_CLOSE, closeable);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether the header should be disabled or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param useHeader or not
	 */
	private void setHeadless(boolean headless) {
		try {
			mCatalogView.put(OPTION_HEADLESS, headless);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether to show out of bounds dialog or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param display or not
	 */
	private void setOutOfBounds(boolean displayOutOfBounds) {
		try {
			mCatalogView.put(OPTION_OUT_OF_BOUNDS, displayOutOfBounds);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Whether to disable branding or not.
	 * Options must be set before {@link #execute() execute()}
	 * @param display or not
	 */
	public void setWhiteLable(boolean displayBranding) {
		try {
			mCatalogView.put(OPTION_WHITE_LABLE, displayBranding);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setViewSession(String viewSession) {
		mViewSession = viewSession;
	}
	
	/**
	 * Callback interface for Pageflip.
	 * Used for callback's on events in the WebView
	 */
	public interface PageflipListener {
		/**
		 * Called when a pageflip event happens.
		 * @param event The type of event
		 * @param data The data received from pageflip
		 */
		public void onEvent(String event, String uuid, JSONObject object);
		public void onReady(String uuid, String viewSession);
	}
	
}