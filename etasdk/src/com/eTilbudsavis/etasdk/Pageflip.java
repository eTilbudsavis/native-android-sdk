/**
 * @fileoverview	Pageflip.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.eTilbudsavis.etasdk;

import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Tools.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public final class Pageflip extends WebView implements Closeable {

	private Eta mETA;

	private static final String EVENT_PREFIX = "eta-pageflip";
	private static final String PF_API_KEY = "apiKey";
	private static final String PF_LAT = "latitude";
	private static final String PF_LNG = "longitude";
	private static final String PF_DISTANCE = "distance";
	private static final String PF_SENSOR = "sensor";
	
	private boolean mIsPageflipInitialized = false;
	private String mType = "";
	private String mContent = "";
	private PageflipListener mListener;
	
	private ArrayList<String> mJSQueue = new ArrayList<String>();
	
	private LinkedHashMap<String, Object> mOptions = new LinkedHashMap<String, Object>();

	/**
	 * ContentType is a simple way of handling what content 
	 * the pageflip should show. 
	 */
	public enum ContentType {
		CATALOG, DEALER
	}

	public Pageflip(Context context) {
		super(context);
	}

	/**
	 * 
	 * @param eta
	 * @param type Whether to show a specific catalog or a list of a dealers catalogs
	 * @param content The ID of the catalog/dealer
	 * @param Listener The listener where callback's will be executed
	 */
	@SuppressLint("DefaultLocale")
	public void execute(Eta eta, ContentType type, String content, PageflipListener Listener) {

		mETA = eta;
		mListener = Listener;
		mType = type.toString().toLowerCase();
		mContent = content;
		
		getSettings().setJavaScriptEnabled(true);
		getSettings().setDefaultTextEncodingName("utf-8");
		
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String[] request = url.split(":", 3);

				// Does the prefix match a pageflip event?
				if (request[0].equals(EVENT_PREFIX)) {
					if (request.length > 2) {
						try {
							final JSONObject object;
							if (request[2].length() == 0) {
								object = new JSONObject();
							} else {

								String resp = "Bad Encoding";
								try {
									resp = URLDecoder.decode(request[2].toString(), "utf-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								object = (resp.equals("Bad Encoding") ? new JSONObject() : new JSONObject(resp) );

								// On first pagechange, execute the JavaScriptQueue.
								if (request[1].toString().equals("pagechange") && object.has("init") && object.getString("init").equals("true")) {
									initializePageflipJS();
								}
							}
							mListener.onPageflipEvent(request[1], object);

						} catch (JSONException e) {
							e.printStackTrace();
						} 
					}

					return true;
				}

				return false;
			}

			// Notify when loading of WebView is done, now insert JavaScript init.
			public void onPageFinished(WebView view, String url) {
				finalizePageflipJS();
			}
		});

		// Check if it's necessary to update the HTML (it's time consuming to download HTML).
		if (mETA.getCache().getHtmlCache() == null ) {
//			mETA.api.request(mETA.getProviderUrl(), new RequestListener() {
//				public void onSuccess(Integer response, Object object) {
//					mETA.setHtmlCached(object.toString());
//					loadData(mETA.getHtmlCached(), "text/html", "UTF-8");
//				}
//
//				public void onError(Integer response, Object object) {
//					loadDataWithBaseURL(null, "<html><body>No internet</body></html>", "text/html", "UTF-8", null);
//				}
//			});
		} else {
			this.loadDataWithBaseURL(null, mETA.getCache().getHtmlCached(), "text/html", "utf-8", null);
		}

	}
	
	// Execute initial options for pageflip
	private void finalizePageflipJS() {
		String s = "";

		LinkedHashMap<String, Object> etaInit = new LinkedHashMap<String, Object>();
		etaInit.put(PF_API_KEY, mETA.getApiKey());
		s += "eta.init(" + Utilities.buildJSString(etaInit) + ");";

		EtaLocation el = mETA.getLocation();
		LinkedHashMap<String, Object> etaloc = new LinkedHashMap<String, Object>();
		etaloc.put(PF_LAT, el.getLatitude());
		etaloc.put(PF_LNG, el.getLongitude());
		etaloc.put(PF_DISTANCE, "0" );
		etaloc.put(PF_SENSOR, "0");
		s += "eta.Location.save(" + Utilities.buildJSString(etaloc) + ");";			

		LinkedHashMap<String, Object> pfinit = new LinkedHashMap<String, Object>();
		pfinit.put(mType, mContent);
		pfinit.put("hotspotsOfferBoundingBox", true);
		pfinit.putAll(mOptions);
		s += "eta.pageflip.init(" + Utilities.buildJSString(pfinit) + ");";
		s += "eta.pageflip.open()";
		execJS(s);
	}
	
	// Execute initial options queue
	private void initializePageflipJS() {
		if (!mJSQueue.isEmpty()) {
			for (String s : mJSQueue) {
				execJS(s);
			}
			mJSQueue.clear();
		}
		mIsPageflipInitialized = true;
	}

	// Actual injection of JS into the WebView
	private void execJS(String option) {
		this.loadUrl("javascript:(function() {" + option + "})()");
	}
	
	/**
	 * Method for updating pageflip location
	 * This will automatically be called when the global ETA location changes
	 */
	public void updateLocation() {
		injectJS("eta.Location.save(" + Utilities.buildJSString(mETA.getLocation().getPageflipLocation()) + ");");
	}
	
	/**
	 * Generic method for setting pageflip options.
	 * If an option isn't available through any other pageflip methods, you can
	 * use this method for setting options in the pageflip.
	 * This method must be called before getWebView-method.
	 *
	 * @param key the option to set
	 * @param value the value of the option
	 */
	public void option(String key, String value) {
		mOptions.put(key, value);
	}
	
	/**
	 * Set the start page of the pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param page number
	 */
	public void setPage(int page) {
		mOptions.put("page", page);
	}
	
	/**
	 * Set hotspots enabled in the pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param enabled or not
	 */
	public void setHotspotsEnabled(boolean value) {
		mOptions.put("hotspotsEnabled", value);
	}
	
	/**
	 * Set header delay for pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param milliseconds of delay
	 */
	public void setHeaderDelay(int seconds) {
		mOptions.put("headerDelay", seconds);
	}
	
	/**
	 * Set swipe threshold for the pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param pixels of threshold
	 */
	public void setSwipeThreshold(int pixels) {
		mOptions.put("swipeThreshold", pixels);
	}
	
	/**
	 * Set the swipe time for the pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param seconds of swipe time
	 */
	public void setSwipeTime(int seconds) {
		mOptions.put("swipeTime", seconds);
	}

	/**
	 * Set the page change animation duration curve for the pageflip.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param center
	 * @param spread
	 * @param height
	 * @param bottom
	 */
	public void setAnimation(int center, int spread, int height, int bottom) {
		LinkedHashMap<String, Object> anim = new LinkedHashMap<String, Object>();

		anim.put("center", center);
		anim.put("spread", spread);
		anim.put("height", height);
		anim.put("bottom", bottom);
		mOptions.put("animation", anim);
	}
	
	/**
	 * Allow us to pick an orientation that works best
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param value, true or false
	 */
	public void setAdaptOrientation(boolean value) {
		mOptions.put("adaptOrientation", value);
	}
	
	/**
	 * whether or not the pageflip is closable.
	 * Options must be set before {@link #execute(ContentType, String, PageflipListener) execute()}
	 *
	 * @param value, true or false
	 */
	public void setClosable(boolean value) {
		mOptions.put("closable", value);
	}
	
	/**
	 * Method for injecting JavaScript into the pageflip
	 * Will first inject JS when WebView has completely loaded the code, 
	 * until then strings will be added to a queue for later injection.
	 *
	 * @param String[] with options to inject
	 * @return True if injected. False if added to queue.
	 */
	public void injectJS(String[] options) {
		for (String string : options)
			injectJS(string);
	}

	/**
	 * Method for injecting JavaScript into the pageflip
	 * Will first inject JS when WebView has completely loaded the code, 
	 * until then strings will be added to a queue for later injection.
	 *
	 * @param String with an option to inject
	 * @return True if injected. False if added to queue.
	 */
	public void injectJS(String option) {
		if (!mIsPageflipInitialized) {
			mJSQueue.add(option);
		}
		execJS(option);
	}
	
	/**
	 * Toggle the thumbnails menu in the WebView.
	 */
	public void toggleThumbnails() {
		injectJS("eta.pageflip.toggleThumbnails();");
	}
	
	/**
	 * Close the pageflip in the WebView.
	 * This ought be called whenever the WebView/pageflip isn't used any more, 
	 * including "onPause", "onStop", "onDestroy".
	 */
	public void close() {
		injectJS("eta.pageflip.close();");
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
		public void onPageflipEvent(String event, JSONObject object);
	}
	
}