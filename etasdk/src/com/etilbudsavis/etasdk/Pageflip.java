/**
 * @fileoverview	Pageflip.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.etilbudsavis.etasdk;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.etilbudsavis.etasdk.API.RequestListener;


@SuppressLint("SetJavaScriptEnabled")
public final class Pageflip {

	private ETA mETA;

	private static final String EVENT_PREFIX = "eta-pageflip";
	private WebView mWebView;
	private boolean mIsPageflipInitialized = false;
	private ArrayList<String> mJSQueue = new ArrayList<String>();
	
	// Setup on getWebView() used in initJS().
	private LinkedHashMap<String, Object> mOptions = new LinkedHashMap<String, Object>();

	/**
	 * ContentType is a simple way of handling what content 
	 * the pageflip should show. 
	 */
	public enum ContentType {
		CATALOG, DEALER
	}

	/**
	 * Constructor for Pageflip.
	 *
	 * @param eta An ETA object containing API key and secret.
	 * @param api If you have created an API object for other purposes
	 * 				 you can include it here to avoid multiple API objects.
	 */
	public Pageflip(WebView webView, ETA eta) {
		mETA = eta;
		mWebView = webView;

		if (!eta.pageflipList.contains(this)) eta.pageflipList.add(this);
	}

	/**
	 * Returns WebView with the desired content, dealer or catalog. The ID of either
	 * catalog or dealer can be found via API calls, see more at
	 * https://etilbudsavis.dk/developers/docs/
	 *
	 * @param type Whether to show a specific catalog or a list of a dealers catalogs
	 * @param content The ID of the catalog/dealer
	 * @param pageflipListener The listener where callback's will be executed
	 * @return The ready-to-go WebView with pageflip enabled
	 */
	public WebView getWebView(ContentType type, String content,
			PageflipListener pageflipListener) {
		final PageflipListener mPageflipListener = pageflipListener;
		final String mType = type.toString().toLowerCase();
		final String mContent = content;
		
		WebSettings mWebSetting = mWebView.getSettings();
		mWebSetting.setJavaScriptEnabled(true);
		mWebSetting.setDefaultTextEncodingName("UTF-8");
		
//		mWebSetting.setAppCacheMaxSize(10*1024*1024);
		
		mWebView.setWebViewClient(new WebViewClient() {
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
								if (request[1].toString().equals("pagechange") && object.has("init")) {
									// two-part if statement to prevent JSONException on "init"
									if (object.getString("init").equals("true")) {
										initQueue();
									}
								}
							}
							mPageflipListener.onPageflipEvent(request[1], object);

						} catch (JSONException e) {
							mPageflipListener.onPageflipEvent(
									"JSONObject parsing error",
									new JSONObject());
							e.printStackTrace();
						} 
					}

					return true;
				}

				return false;
			}

			// Notify when loading of WebView is done, now insert JavaScript
			// init.
			public void onPageFinished(WebView view, String url) {
				initJS(mType, mContent);
			}
		});

		// Check if it's necessary to update the HTML (it's time consuming to
		// download HTML).
		if (mETA.getHtmlCached().length() == 0 || (Utilities.getTime() - mETA.getHtmlAcquired()) >= mETA.getHtmlExpire()) {
			mETA.api.request(mETA.getProviderUrl(), new RequestListener() {
				public void onSuccess(Integer response, Object object) {
					mETA.setHtmlCached(object.toString());
					mWebView.loadData(mETA.getHtmlCached(), "text/html", "UTF-8");
				}

				public void onError(Integer response, Object object) {
					mWebView.loadDataWithBaseURL(null, "<html><body>No internet</body></html>", "text/html", "UTF-8", null);
				}
			});
		} else {
			mWebView.loadDataWithBaseURL(null, mETA.getHtmlCached(), "text/html", "UTF-8", null);
		}

		
		Utilities.logd("Pageflip", "Test1,3");

		return mWebView;
	}
	
	public WebView getRestoredWebView() {

			return mWebView;

	}
	
	// Execute initial options for pageflip
	private void initJS(String type, String content) {
		String s = "";
		
		LinkedHashMap<String, Object> etaInit = new LinkedHashMap<String, Object>();
			etaInit.put("apiKey", mETA.getApiKey());
			etaInit.put("apiSecret", mETA.getApiSecret());
			etaInit.put("uuid", mETA.getUUID());
		s += "eta.init(" + Utilities.buildJSString(etaInit) + ");";
		
		if (mETA.getLocation().useLocation()) {
			Bundle loc = mETA.getLocation().getLocationAsApiParams();
			LinkedHashMap<String, Object> etaloc = new LinkedHashMap<String, Object>();
				etaloc.put("latitude", loc.getDouble("api_latitude"));
				etaloc.put("longitude", loc.getDouble("api_longitude"));
				etaloc.put("distance", mETA.getLocation().useDistance() ? loc.getInt("api_distance") : "0" );
				etaloc.put("locationDetermined", loc.getInt("api_locationDetermined"));
				etaloc.put("geocoded", loc.getInt("api_geocoded"));
				if (loc.getInt("api_geocoded") == 0) 
					etaloc.put("accuracy", loc.getInt("api_accuracy"));
			s += "eta.Location.save(" + Utilities.buildJSString(etaloc) + ");";			
		}
		
		LinkedHashMap<String, Object> pfinit = new LinkedHashMap<String, Object>();
			pfinit.put(type, content);
			pfinit.put("hotspotsOfferBoundingBox", true);
			pfinit.putAll(mOptions);
		s += "eta.pageflip.init(" + Utilities.buildJSString(pfinit) + ");";
		s += "eta.pageflip.open()";
		execJS(s);
	}
	
	// Execute initial queue
	private void initQueue() {
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
		mWebView.loadUrl("javascript:(function() {" + option + "})()");
	}
	
	/**
	 * Method for updating pageflip location
	 * This will automatically be called on ETA.location.setLocation()
	 */
	public boolean updateLocation() {
		if (mETA.getLocation().useLocation()) {
			return injectJS("eta.Location.save(" + Utilities.buildJSString(mETA.getLocation().getPageflipLocation()) + ");");
		}
		return false;
	}
	
	/**
	 * Generic method for setting pageflip options.
	 * If an option isn't available through any other
	 * pageflip methods, you can use this method for setting
	 * options in the pageflip.
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
	 * This method must be called before getWebView-method.
	 *
	 * @param page number
	 */
	public void setPage(int page) {
		mOptions.put("page", page);
	}
	
	/**
	 * Set hotspots enabled in the pageflip.
	 * This method must be called before getWebView-method.
	 *
	 * @param enabled or not
	 */
	public void setHotspotsEnabled(boolean value) {
		mOptions.put("hotspotsEnabled", value);
	}
	
	/**
	 * Set header delay for pageflip.
	 * This method must be called before getWebView-method.
	 *
	 * @param milliseconds of delay
	 */
	public void setHeaderDelay(int seconds) {
		mOptions.put("headerDelay", seconds);
	}
	
	/**
	 * Set swipe threshold for the pageflip.
	 * This method must be called before getWebView-method.
	 *
	 * @param pixels of threshold
	 */
	public void setSwipeThreshold(int pixels) {
		mOptions.put("swipeThreshold", pixels);
	}
	
	/**
	 * Set the swipe time for the pageflip.
	 * This method must be called before getWebView-method.
	 *
	 * @param seconds of swipe time
	 */
	public void setSwipeTime(int seconds) {
		mOptions.put("swipeTime", seconds);
	}

	/**
	 * Set the page change animation duration curve for the pageflip.
	 * This method must be called before getWebView-method.
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
	 * This method must be called before getWebView-method.
	 *
	 * @param value, true or false
	 */
	public void setAdaptOrientation(boolean value) {
		mOptions.put("adaptOrientation", value);
	}
	
	/**
	 * whether or not the pageflip is closable.
	 * This method must be called before getWebView-method.
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
	public boolean injectJS(String[] options) {
		boolean injected = false;

		for (String string : options) {
			injected = injectJS(string);
		}

		return injected;
	}

	/**
	 * Method for injecting JavaScript into the pageflip
	 * Will first inject JS when WebView has completely loaded the code, 
	 * until then strings will be added to a queue for later injection.
	 *
	 * @param String with an option to inject
	 * @return True if injected. False if added to queue.
	 */
	public boolean injectJS(String option) {
		if (!mIsPageflipInitialized) {
			mJSQueue.add(option);
			return false;
		} 

		execJS(option);

		return true;
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