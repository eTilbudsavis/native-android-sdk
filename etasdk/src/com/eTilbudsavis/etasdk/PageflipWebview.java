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
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressLint("SetJavaScriptEnabled")
public final class PageflipWebview extends WebView {

	private static final String TAG = "Pageflip";

	private static final String PAGEFLIP_PROXY_NAME = "AndroidInterface";

	private static final String EVENT_PROXY_READY = "eta-proxy-ready";
	
	private static final String EVENT_SESSION_CHANGE = "eta-session-change";
	
	private static final String EVENT_API_REQUEST = "eta-api-request";
	
	private static final String API_REQUEST_COMPLETE = "api-request-complete";
	

	private static final String COMMAND_CONFIGURE = "configure";
	private static final String OVERRIDE_API = "overrideAPI";
	
	private static final String COMMAND_INITIALIZE = "initialize";
	private static final String INIT_API_KEY = "apiKey";
	private static final String INIT_API_SECRET = "apiSecret";
	private static final String INIT_SESSION = "session";
	private static final String INIT_TOKEN_TTL = "tokenTTL";
	private static final String INIT_LOCALE = "locale";
	private static final String INIT_LOCATION = "geolocation";
	private static final String INIT_LOCATION_LAT = "latitude";
	private static final String INIT_LOCATION_LNG = "longitude";
	private static final String INIT_LOCATION_SENSOR = "sensor";
	private static final String INIT_LOCATION_RADIUS = "radius";
	private static final String INIT_USERAGENT = "userAgent";
	
	/** String identifying the option to toggle visibility of the thumbnail view, in pageflip. @see {@link #toggleThumbnails() toggleThumbnails()}*/
	public static final String CATALOG_VIEW_THUMBNAILS = "catalog-view-thumbnails";
	/** String identifying the option to go to a certain page in pageflip @see {@link #gotoPage(int) gotoPage()}*/
	public static final String CATALOG_VIEW_GOTO_PAGE = "catalog-view-go-to-page";
	/** String identifying the option to close the JavaScript inside PageflipWebview */
	public static final String CATALOG_VIEW_CLOSE = "catalog-view-close";
	
	public static final String GEOLOCATION_CHANGE = "geolocation-change";
	
	/** String identifying the catalog-view parameter, used when loading a catalog. All available options for this parameter is prefixed with "OPTION" as in {@link PageflipWebview#OPTION_CATALOG OPTION_CATALOG} */
	private static final String COMMAND_CATALOG_VIEW = "catalog-view";
	/** String identifying the catalog option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_CATALOG = "catalog";
	/** String identifying the page option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_PAGE = "page";
	/** String identifying the hotspots option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HOTSPOTS = "hotspots";
	/** String identifying the hotspot overlay option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HOTSPOT_OVERLAY = "hotspotOverlay";
	/** String identifying the can close option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_CAN_CLOSE = "canClose";
	/** String identifying the headless option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_HEADLESS = "headless";
	/** String identifier for the put of bounds option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details */
	public static final String OPTION_OUT_OF_BOUNDS = "outOfBounds";
	/** String identifying the white lable option, used when setting options with the parameter {@link PageflipWebview#COMMAND_CATALOG_VIEW PARAM_CATALOG_VIEW}. See pageflip documentation for more details<br>
	 * <b>NOTE</b> The usage of this option can violate the terms of use. */
	private static final String OPTION_WHITE_LABLE = "whiteLabel";
	/** String identifier for session change option */
	private static final String OPTION_SESSION_CHANGE = "session-change";
	
	/** String identifying the pagechange pageflip event. See pageflip documentation for more details */
	public static final String EVENT_PAGECHANGE = "eta-catalog-view-pagechange";
	/** String identifying the outofbounds pageflip event. See pageflip documentation for more details */
	public static final String EVENT_OUTOFBOUNDS = "eta-catalog-view-outofbounds";
	/** String identifying the hotspot pageflip event. See pageflip documentation for more details */
	public static final String EVENT_HOTSPOT = "eta-catalog-view-hotspot";
	/** String identifying the singletap pageflip event. See pageflip documentation for more details */
	public static final String EVENT_SINGLETAP = "eta-catalog-view-singletap";
	/** String identifying the doubletap pageflip event. See pageflip documentation for more details */
	public static final String EVENT_DOUBLETAP = "eta-catalog-view-doubletap";
	/** String identifying the dragstart pageflip event. See pageflip documentation for more details */
	public static final String EVENT_DRAGSTART = "eta-catalog-view-dragstart";
	/** String identifying en error event. */
	public static final String EVENT_ERROR = "eta-catalog-view-error";
	
	private Eta mEta;
	private String mUuid;
	private PageflipListener mListener;
	private String mCatalogId;
	private JSONObject mCatalogViewOptions = new JSONObject();
	private boolean mThumbnailsToggled = false;
	private boolean mReady = false;
	
	/** The interface between Webview And Pageflip */
	private PageflipJavaScriptInterface mPFInterface;
	
	/** To avoid recursion, when Pageflip updates session */
	private boolean mSessionFromPageflip = false;
	
	/**
	 * List of all active pageflips. 
	 * SDK uses this list to progagate session- and location changes into the pageflips
	 */
	public static List<PageflipWebview> pageflips = new ArrayList<PageflipWebview>();
	
	/**
	 * Used for manual inflation
	 * @param context
	 */
	public PageflipWebview(Context context) {
		super(context);
		setHeadless(true);
        setOutOfBounds(false);
	}
	
	/**
	 * Constructor used when inflating Pageflip from XML.
	 * @param context
	 * @param attrs
	 */
	public PageflipWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
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
		
		mEta = eta;
		mListener = Listener;
		mCatalogId = CatalogId;
		mUuid = Utils.createUUID();
		mPFInterface = new PageflipJavaScriptInterface();
		
		pageflips.add(this);
		
		WebSettings ws = getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setDefaultTextEncodingName("utf-8");
		ws.setRenderPriority(RenderPriority.HIGH);
		ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
//		if (Eta.DEBUG_PAGEFLIP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//		    WebView.setWebContentsDebuggingEnabled(true);
//		}
		
		ws.setAllowFileAccess(true);
		
		addJavascriptInterface(mPFInterface, PAGEFLIP_PROXY_NAME);
		
		if (Eta.DEBUG_PAGEFLIP) {
			setWebChromeClient(wcc);
		}
		
		String url = Request.Endpoint.pageflipProxy(mUuid);
		EtaLog.d(TAG, url);
		StringRequest req = new StringRequest(url, new Listener<String>() {

			public void onComplete(boolean isCache, String response, EtaError error) {
				
				if (!isCache) {
					
					if (response == null) {
						loadDataWithBaseURL(null, "<html><body>" + error.toString() + "</body></html>", "text/html", "utf-8", null);
						mListener.onEvent(EVENT_ERROR, mUuid, error.toJSON());
					} else {
						loadDataWithBaseURL(null, response, "text/html", "utf-8", null);
					}
					
				}
				
			}
		});
		
		req.cacheOnlyifExists(true);
		req.debugNetwork(true);
		mEta.add(req);
		
	}
	
	private JSONObject location() {
		JSONObject loc = new JSONObject();
		try {
			loc.put(INIT_LOCATION_LAT, mEta.getLocation().getLatitude());
			loc.put(INIT_LOCATION_LNG, mEta.getLocation().getLongitude());
			loc.put(INIT_LOCATION_SENSOR, mEta.getLocation().isSensor());
			loc.put(INIT_LOCATION_RADIUS, mEta.getLocation().getRadius());
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
			config.put(OVERRIDE_API, true);
			mPFInterface.etaProxy(COMMAND_CONFIGURE, config);
			
			// Inject catalog, and device specific info
			JSONObject init = new JSONObject();
			init.put(INIT_API_KEY, mEta.getApiKey());
			init.put(INIT_API_SECRET, mEta.getApiSecret());
			JSONObject ses = mEta.getSessionManager().getSession().toJSON();
			init.put(INIT_SESSION, ses);
			init.put(INIT_LOCALE, Locale.getDefault().toString());
			init.put(INIT_USERAGENT, mEta.getAppVersion());
			if (mEta.getLocation().isSet()) {
				init.put(INIT_LOCATION, location());
			}
			mPFInterface.etaProxy(COMMAND_INITIALIZE, init);
			
			// Inject view options
			mCatalogViewOptions.put(OPTION_CATALOG, mCatalogId);
			mPFInterface.etaProxy(COMMAND_CATALOG_VIEW, mCatalogViewOptions);
		
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
	}

	private LocationListener ll = new LocationListener() {
		
		public void onChange() {
			mPFInterface.etaProxy(GEOLOCATION_CHANGE, location());
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
		mPFInterface.etaProxy(CATALOG_VIEW_THUMBNAILS);
	}
	
	public void updateSession() {
		if (!mSessionFromPageflip) {
			Session s = mEta.getSessionManager().getSession();		
			mPFInterface.etaProxy(OPTION_SESSION_CHANGE, s.toJSON());
		}
		mSessionFromPageflip = false;
	}
	
	public void closePageflip() {
		if (mPFInterface != null) {
			Eta.getInstance().getLocation().unSubscribe(ll);
			pageflips.remove(PageflipWebview.this);
			mPFInterface.etaProxy(CATALOG_VIEW_CLOSE);
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
			mPFInterface.etaProxy(CATALOG_VIEW_GOTO_PAGE, data);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
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
			mCatalogViewOptions.put(key, value);
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
			mCatalogViewOptions.put(OPTION_PAGE, page);
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
			mCatalogViewOptions.put(OPTION_HOTSPOTS, enabled);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method for determining if the pageflip is ready.
	 * @return true if the catalog is ready
	 */
	public boolean isReady() {
		return mReady;
	}
	
	/**
	 * Whether to disable hotspot overlay or not.<br>
	 * Options must be set before {@link #execute() execute()}
	 * @param enabled or not
	 */
	public void setHotspotOverlayVisible(boolean visible) {
		try {
			mCatalogViewOptions.put(OPTION_HOTSPOT_OVERLAY, visible);
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
			mCatalogViewOptions.put(OPTION_CAN_CLOSE, closeable);
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
			mCatalogViewOptions.put(OPTION_HEADLESS, headless);
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
			mCatalogViewOptions.put(OPTION_OUT_OF_BOUNDS, displayOutOfBounds);
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
			mCatalogViewOptions.put(OPTION_WHITE_LABLE, displayBranding);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public class PageflipJavaScriptInterface {
		
		HashMap<String, JSONObject> mResponses = new HashMap<String, JSONObject>();
		
		@JavascriptInterface
		public void dispatch(String payload) {
			
			try {
				
				JSONObject tmp = new JSONObject(payload);
				final String event = tmp.getString("eventName");
				final JSONObject data = tmp.getJSONObject("data");
				
				// Some types of events, doesn't need to go to the UI thread.
				if (event.equals(EVENT_SESSION_CHANGE) || event.equals(EVENT_PROXY_READY) || event.equals(EVENT_API_REQUEST)) {
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
			
			final String s = String.format("javascript:(function() { %s })()", jsCommand);
			
			mEta.getHandler().post(new Runnable() {
				
				public void run() {
					loadUrl(s);
				}
			});
		}
		
		public void onEvent(String event, JSONObject data){
			
			EtaLog.d(TAG, "onEvent: " + event);
			
			if (event.equals(EVENT_SESSION_CHANGE)) {
				
				mSessionFromPageflip = true;
				mEta.getSessionManager().setSession(data);
				
			} else if (event.equals(EVENT_PROXY_READY)) {
				
				initializeJavaScript();
				
			} else if (event.equals(EVENT_API_REQUEST)) {
				
				try {
					
					JSONObject request = data.getJSONObject("data");
					
					final String rId = data.getString("id");
					final String rUrl = request.getString("url");
					
					String rBody = null;
					if (request.has("data")) {
						rBody = request.getString("data");
					}
					
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
						
						public void onComplete( boolean isCache, String response, EtaError error) {

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
								
								etaProxy(rId, API_REQUEST_COMPLETE, resp);
								
							} catch (JSONException e) {
								EtaLog.d(TAG, e);
							}
							
						}
					});
					mEta.add(req);
					
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
				
			} else {
				
				if (event.equals(EVENT_PAGECHANGE)) {
					
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
		 * Called when a pageflip event happens.
		 * @param event The type of event
		 * @param data The data received from pageflip
		 */
		public void onEvent(String event, String uuid, JSONObject object);
		public void onReady(String uuid);
	}
	
}