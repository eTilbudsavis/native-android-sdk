/*!
 * eTilbudsavis ApS
 * (c) 2012, eTilbudsavis ApS
 * http://etilbudsavis.dk
 */
/**
 * @fileoverview	Main class.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Network.EtaCache;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String TAG = "ETA";
	
	public static boolean DEBUG_LOGD = false;
	public static boolean DEBUG_PAGEFLIP = false;
	
	public static boolean USE_FALSE_TOKEN = true;
	
	private static Eta mEta;
	
	private Context mContext;
	private String mApiKey;
	private String mApiSecret;
	private String mAppVersion;
	private Settings mSettings;
	private SessionManager mSessionManager;
	private EtaLocation mLocation;
	private EtaCache mCache;
	private ListManager mListManager;
	private static Handler mHandler;
	private ExecutorService mThreads = Executors.newFixedThreadPool(10);
	private boolean mResumed = false;
	private ConnectivityManager mConnectivityManager;
	
	private Eta() { }

	/**
	 * TODO: Write a long story about usage, this will basically be the documentation
	 * @param apiKey The API key found at http://etilbudsavis.dk/api/
	 * @param apiSecret The API secret found at http://etilbudsavis.dk/api/
	 * @param context The context of the activity instantiating this class.
	 */
	public static Eta getInstance() {
		if (mEta == null) {
			synchronized (Eta.class) {
				if (mEta == null) {
					mEta = new Eta();
				}
			}
		}
		return mEta;
	}
	
	/**
	 * Use this method to do the initial instanciation of the Eta object.<br>
	 * If you do not the SDK will not function.
	 * @param apiKey for your app
	 * @param apiSecret for your app
	 * @param context for your app
	 */
	public void set(String apiKey, String apiSecret, Context context) {
		
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		
		if (!isSet()) {
			mSettings = new Settings(mContext);
			mLocation = new EtaLocation(Eta.this);
			mCache = new EtaCache();
			mListManager = new ListManager(Eta.this);
			mSessionManager = new SessionManager(this);
		} else {
			EtaLog.d(TAG, "Eta already set. apiKey, apiSecret and context has been switched");
		}
		
	}
	
	/**
	 * Method for checking if the Eta object have been set.<br>
	 * note: Nothing happens if Eta.set() is called multiple times.
	 * @return
	 */
	public boolean isSet() {
		return mApiKey != null && mApiSecret == null;
	}
	
	public boolean isOnline() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}
	
	/**
	 * The context, the given Eta has been set in.<br>
	 * This context, does not necessarily have real estate on screen
	 * to instantiate any views.
	 * @return A context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Returns the API key found at http://etilbudsavis.dk/api/.
	 * @return API key as String
	 */
	public String getApiKey() {
		return mApiKey;
	}

	/**
	 * Set the version of your app, for better statistics collection on http://insight.etilbudsavis.dk/.<br><br>
	 * App version should follow http://semver.org/ specifications (MAJOR.MINOR.PATCH), e.g.:<br>
	 * <li> 1.0.0
	 * <li> 1.0.0-beta
	 * <li> 1.0.0-rc.1
	 * 
	 * @return API key as String
	 */
	public void setAppVersion(String appVersion) {
		mAppVersion = appVersion;
	}

	/**
	 * Set the version of your app, for better statistics collection on http://insight.etilbudsavis.dk/.<br><br>
	 * App version should follow http://semver.org/ specifications (MAJOR.MINOR.PATCH), e.g.:<br>
	 * <li> 1.0.0
	 * <li> 1.0.0-beta
	 * <li> 1.0.0-rc.1
	 * 
	 * @return API key as String
	 */
	public String getAppVersion() {
		return mAppVersion;
	}
	
	/**
	 * Returns the API secret found at http://etilbudsavis.dk/api/.
	 * @return API secret as String
	 */
	public String getApiSecret() {
		return mApiSecret;
	}

	/**
	 * Get the currently active session.
	 * @return a session
	 */
	public SessionManager getSessionManager() {
		return mSessionManager;
	}

	/**
	 * Get the current user
	 * @return a user
	 */
	public User getUser() {
		return mSessionManager.getSession().getUser();
	}
	
	/**
	 * Get the settings, that ETA SDK is using.
	 */
	public Settings getSettings() {
		return mSettings;
	}

	/**
	 * A location object used by ETA, when making API requests.
	 * This object should be edited when ever you want to change location.
	 * @return <li> A location object
	 */
	public EtaLocation getLocation() {
		return mLocation;
	}

	/**
	 * Get the ETA SDK cache for various items and objects.
	 * @return the current cache object
	 */
	public EtaCache getCache() {
		return mCache;
	}
	
	/**
	 * Gets the current ShoppinglistManager.<br>
	 * Use this manager for all shopping list relevant operations to ensure data consistency.
	 * @return an instance of ShoppinglistManager
	 */
	public ListManager getListManager() {
		return mListManager;
	}
	
	/**
	 * Get a static handler, created on the main looper. <br>
	 * Use this to avoid memory leaks.
	 * @return a handler
	 */
	public Handler getHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper());
		}
		return mHandler;
	}
	
	/**
	 * Returns an ExecutorService (thread pool), used by the Api.<br>
	 * Using a custom thread pool, rather than AsyncTast, is way better for
	 * parallel actions, as the AsyncTash sometimes only has one thread (sequential execution).
	 * @return ExecutorService used by Api class
	 */
	public ExecutorService getThreadPool() {
		return mThreads;
	}
	
	/**
	 * Simply instantiates and returns a new Api object.
	 * @return a new Api object
	 */
	public Api getApi() {
		return new Api(this);
	}
	
	/**
	 * Clears ALL preferences that the SDK has created.<br><br>
	 * 
	 * This includes the session and user.
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	public boolean clearPreferences() {
		mSessionManager.invalidate();
		mListManager.clear();
		return mSettings.clear();
	}

	/**
	 * The debug mode will allow for printing of Exceptions e.t.c. to LogCat.
	 * @param useDebug true if Eta hsould be in debug mode
	 * @return this object
	 */
	public Eta debug(boolean useDebug) {
		DEBUG_LOGD = useDebug;
		return this;
	}

	/**
	 * Is the SDK in debug mode?
	 * @return true if eta is in debug
	 */
	public boolean isDebug() {
		return DEBUG_LOGD;
	}
	
	/**
	 * Method for querying for the current life cycle state of the app.
	 * @return true if resumed
	 */
	public boolean isResumed() {
		return mResumed;
	}
	
	/**
	 * Method to call on Activity.onPause().
	 * This method will clean up the SDK.
	 */
	@SuppressLint("NewApi")
	public void onPause() {
		if (mResumed) {
			mResumed = false;
			mLocation.saveState();
			mListManager.onPause();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onPause();
		}
	}
	
	/**
	 * Method to call on Activity.onResume().<br>
	 * This method will resume all SDK relevant stuff.
	 */
	@SuppressLint("NewApi")
	public void onResume() {
		if (!mResumed) {
			mResumed = true;
			mLocation.restoreState();
			mListManager.onResume();
			mSessionManager.onResume();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onResume();
		}
	}
	
	private Bundle getApiParams(int offset, int limit, String orderBy) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Request.Param.OFFSET, offset);
		apiParams.putInt(Request.Param.LIMIT, limit);
		if (orderBy != null) 
			apiParams.putString(Request.Sort.ORDER_BY, orderBy);
		return apiParams;
	}
	
	private Bundle getSearchApiParams(int offset, int limit, String orderBy, String query) {
		Bundle apiParams = getApiParams(offset, limit, orderBy);
		apiParams.putString(Request.Param.QUERY, query);
		return apiParams;
	}

//  ****************
//	*   CATALOGS   *
//	****************
	
	public Api getCatalogList(Api.ListListener<Catalog> listener) {
		return getCatalogList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getCatalogList(Api.ListListener<Catalog> listener, int offset, int limit) {
		return getApi().get(Catalog.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getCatalogList(Api.ListListener<Catalog> listener, int offset, int limit, String orderBy) {
		Api a = getApi().get(Catalog.ENDPOINT_LIST, listener).setOffset(offset).setLimit(limit);
		if (orderBy != null) a.setOrderBy(orderBy);
		return a;
	}

	public Api getCatalogFromId(Api.ItemListener<Catalog> listener, String catalogId) {
		return getApi().get(Catalog.ENDPOINT_ID, listener).setId(catalogId);
	}

	public Api getCatalogFromIds(Api.ListListener<Catalog> listener, Set<String> catalogIds) {
		return getApi().get(Catalog.ENDPOINT_LIST, listener).setCatalogIds(catalogIds);
	}

	public Api searchCatalogss(Api.ListListener<Catalog> listener, String query) {
		return searchCatalogss(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchCatalogss(Api.ListListener<Catalog> listener, String query, int offset, int limit, String orderBy) {
		return getApi().get(Catalog.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

//  **************
//	*   OFFER    *
//	**************
	
	public Api getOfferList(Api.ListListener<Offer> listener) {
		return getOfferList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferList(Api.ListListener<Offer> listener, int offset, int limit) {
		return getApi().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getOfferList(Api.ListListener<Offer> listener, int offset, int limit, String orderBy) {
		return getApi().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getOfferFromId(Api.ItemListener<Offer> listener, String offerId) {
		return getApi().get(Offer.ENDPOINT_ID, listener, new Bundle()).setId(offerId);
	}
	
	public Api getOfferFromIds(Api.ListListener<Offer> listener, Set<String> offerIds) {
		return getApi().get(Offer.ENDPOINT_LIST, listener, new Bundle()).setOfferIds(offerIds);
	}
	
	public Api searchOffers(Api.ListListener<Offer> listener, String query) {
		return searchOffers(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchOffers(Api.ListListener<Offer> listener, String query, int offset, int limit) {
		return searchOffers(listener, query, offset, limit, null);
	}

	public Api searchOffers(Api.ListListener<Offer> listener, String query, int offset, int limit, String orderBy) {
		return getApi().get(Offer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

//  **************
//	*   DEALER   *
//	**************
	
	public Api getDealerList(Api.ListListener<Dealer> listener) {
		return getDealerList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerList(Api.ListListener<Dealer> listener, int offset, int limit) {
		return getApi().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getDealerList(Api.ListListener<Dealer> listener, int offset, int limit, String orderBy) {
		return getApi().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getDealerFromId(Api.ItemListener<Dealer> listener, String dealerId) {
		return getApi().get(Dealer.ENDPOINT_ID, listener, new Bundle()).setId(dealerId);
	}
	
	public Api getDealerFromIds(Api.ListListener<Dealer> listener, Set<String> dealerIds) {
		return getApi().get(Dealer.ENDPOINT_LIST, listener, new Bundle()).setDealerIds(dealerIds);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query) {
		return searchDealers(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query, int offset, int limit) {
		return searchDealers(listener, query, offset, limit, null);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query, int offset, int limit, String orderBy) {
		return getApi().get(Dealer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

//  **************
//	*   STORES   *
//	**************
	
	public Api getStoreList(Api.ListListener<Store> listener) {
		return getStoreList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreList(Api.ListListener<Store> listener, int offset, int limit) {
		return getApi().get(Store.ENDPOINT_LIST, listener).setOffset(offset).setLimit(limit);
	}

	public Api getStoreList(Api.ListListener<Store> listener, int offset, int limit, String orderBy) {
		return getApi().get(Store.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getStoreFromId(Api.ItemListener<Store> listener, String storeId) {
		return getApi().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(storeId);
	}
	
	public Api getStoreFromIds(Api.ListListener<Store> listener, Set<String> storeIds) {
		return getApi().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(storeIds);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query) {
		return searchStores(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query, int offset, int limit) {
		return searchStores(listener, query, offset, limit, null);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query, int offset, int limit, String orderBy) {
		return getApi().get(Store.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

}