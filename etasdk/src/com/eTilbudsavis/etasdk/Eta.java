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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Network.EtaCache;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Sort;
import com.eTilbudsavis.etasdk.Utils.Timer;
import com.eTilbudsavis.etasdk.Utils.Utils;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String TAG = "ETA";
	public static boolean DEBUG = false;
	private List<Pageflip> mPageflips = new ArrayList<Pageflip>();
	
	private static Eta mEta;
	
	private Context mContext;
	private String mApiKey;
	private String mApiSecret;
	private Settings mSettings;
	private Session mSession;
	private EtaLocation mLocation;
	private EtaCache mCache;
	private ShoppinglistManager mShoppinglistManager;
	private static Handler mHandler = new Handler();
	private ExecutorService mThreads = Executors.newFixedThreadPool(10);
	private boolean mResumed = false;
	
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
	
	public void set(String apiKey, String apiSecret, Context context) {

		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		
		if (!isSet()) {
			mSettings = new Settings(mContext);
			mLocation = new EtaLocation();
			mCache = new EtaCache();
			mShoppinglistManager = new ShoppinglistManager(Eta.this);
			mSession = new Session(Eta.this);
			mSession.init();
		} else {
			Utils.logd(TAG, "Eta already set. apiKey, apiSecret and context has been switched");
		}
		
	}
	
	public boolean isSet() {
		return mApiKey != null && mApiSecret == null;
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
	public Session getSession() {
		return mSession;
	}

	/**
	 * Get the current user
	 * @return a user
	 */
	public User getUser() {
		return getSession().getUser();
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
	 * @return 
	 */
	public EtaCache getCache() {
		return mCache;
	}
	
	public ShoppinglistManager getShoppinglistManager() {
		return mShoppinglistManager;
	}
	
	/**
	 * Get a static handler, to avoid memory leaks.
	 * @return a handler
	 */
	public Handler getHandler() {
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
		mSession = new Session(Eta.this);
		return mSettings.clear();
	}

	public boolean isDebug() {
		return DEBUG;
	}
	
	public boolean isResumed() {
		return mResumed;
	}
	
	public Eta debug(boolean useDebug) {
		DEBUG = useDebug;
		return this;
	}

	public void onPause() {
		if (mResumed) {
			mResumed = false;
			mShoppinglistManager.onPause();
			mLocation.saveState();
			pageflipPause();
		}
	}
	
	public void onResume() {
		if (!mResumed) {
			mResumed = true;
			mShoppinglistManager.onResume();
			mLocation.restoreState();
			pageflipResume();
		}
	}

	// Why does it keep bothering me about, API v11 when i've implemented the method?
	@SuppressLint("NewApi")
	private void pageflipResume() {
		for (Pageflip p : mPageflips)
			p.onResume();
	}

	// Why does it keep bothering me about, API v11 when i've implemented the method?
	@SuppressLint("NewApi")
	private void pageflipPause() {
		for (Pageflip p : mPageflips)
			p.onPause();
	}
	
	private Bundle getApiParams(int offset, int limit, String orderBy) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Params.OFFSET, offset);
		apiParams.putInt(Params.LIMIT, limit);
		if (orderBy != null) 
			apiParams.putString(Sort.ORDER_BY, orderBy);
		return apiParams;
	}
	
	private Bundle getSearchApiParams(int offset, int limit, String orderBy, String query) {
		Bundle apiParams = getApiParams(offset, limit, orderBy);
		apiParams.putString(Params.QUERY, query);
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