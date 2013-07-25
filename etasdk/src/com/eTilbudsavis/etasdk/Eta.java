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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Sort;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Debug tag */
	public static final String TAG = "ETA";
	
	/** 
	 * Variable to decide whether to show debug log messages.<br><br>
	 * Please only set to <code>true</code> while developing to avoid leaking sensitive information */
	public static boolean DEBUG = false;
	
	/** The date format as returned from the server */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+SSSS";
	
	/** Name for the SDK SharedPreferences file */
	public static final String PREFS_NAME = "eta_sdk";
	
	private Context mContext;
	private final String mApiKey;
	private final String mApiSecret;
	private Session mSession;
	private SharedPreferences mPrefs;
	private EtaLocation mLocation;
	private EtaCache mCache;
	private ShoppinglistManager mShoppinglistManager;
	private static Handler mHandler = new Handler();
	private ArrayList<Pageflip> mPageflips = new ArrayList<Pageflip>();
	private ExecutorService mThreads = Executors.newFixedThreadPool(10);
	
	/**
	 * TODO: Write a long story about usage, this will basically be the documentation
	 * @param apiKey The API key found at http://etilbudsavis.dk/api/
	 * @param apiSecret The API secret found at http://etilbudsavis.dk/api/
	 * @param context The context of the activity instantiating this class.
	 */
	public Eta(String apiKey, String apiSecret, Context context) {
		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mLocation = new EtaLocation(mPrefs);
		mCache = new EtaCache();
		mSession = new Session(this);
		mSession.start();
		mShoppinglistManager = new ShoppinglistManager(this);
	}

	/**
	 * Returns the Context
	 * @return API key as String
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
	 * Get the SharedPreferences, that ETA SDK is using.
	 */
	public SharedPreferences getPrefs() {
		return mPrefs;
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
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public ExecutorService getThreadPool() {
		return mThreads;
	}
	/**
	 * Simply instantiates and returns a new Api object.
	 * @return a new Api object
	 */
	public Api api() {
		return new Api(this);
	}
	
	/**
	 * Clears ALL preferences that the SDK has created.<br><br>
	 * 
	 * This includes the session and user.
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	public boolean clearPreferences() {
		mSession = new Session(this);
		return mPrefs.edit().clear().commit();
	}

	public boolean isDebug() {
		return DEBUG;
	}

	public Eta debug(boolean useDebug) {
		DEBUG = useDebug;
		return this;
	}

	public Eta addPageflip(Pageflip p) {
		mPageflips.add(p);
		return this;
	}

	public Eta removePageflip(Pageflip p) {
		mPageflips.remove(p);
		return this;
	}
	
	public void onPause() {
		mShoppinglistManager.onPause();
		pageflipPause();
	}
	
	public void onResume() {
		mShoppinglistManager.onResume();
		pageflipResume();
	}

	private void pageflipLocation() {
		for (Pageflip p : mPageflips)
			p.resume();
	}
	
	private void pageflipResume() {
		for (Pageflip p : mPageflips)
			p.resume();
	}
	
	private void pageflipPause() {
		for (Pageflip p : mPageflips)
			p.pause();
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
		return api().get(Catalog.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getCatalogList(Api.ListListener<Catalog> listener, int offset, int limit, String orderBy) {
		Api a = api().get(Catalog.ENDPOINT_LIST, listener).setOffset(offset).setLimit(limit);
		if (orderBy != null) a.setOrderBy(orderBy);
		return a;
	}

	public Api getCatalogFromId(Api.ItemListener<Catalog> listener, String catalogId) {
		return api().get(Catalog.ENDPOINT_ID, listener).setId(catalogId);
	}

	public Api getCatalogFromIds(Api.ListListener<Catalog> listener, Set<String> catalogIds) {
		return api().get(Catalog.ENDPOINT_LIST, listener).setCatalogIds(catalogIds);
	}

//  **************
//	*   OFFER    *
//	**************
	
	public Api getOfferList(Api.ListListener<Offer> listener) {
		return getOfferList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferList(Api.ListListener<Offer> listener, int offset, int limit) {
		return api().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getOfferList(Api.ListListener<Offer> listener, int offset, int limit, String orderBy) {
		return api().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getOfferFromId(Api.ItemListener<Offer> listener, String offerId) {
		return api().get(Offer.ENDPOINT_ID, listener, new Bundle()).setId(offerId);
	}
	
	public Api getOfferFromIds(Api.ListListener<Offer> listener, Set<String> offerIds) {
		return api().get(Offer.ENDPOINT_LIST, listener, new Bundle()).setOfferIds(offerIds);
	}
	
	public Api searchOffers(Api.ListListener<Offer> listener, String query) {
		return searchOffers(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchOffers(Api.ListListener<Offer> listener, String query, int offset, int limit) {
		return searchOffers(listener, query, offset, limit, null);
	}

	public Api searchOffers(Api.ListListener<Offer> listener, String query, int offset, int limit, String orderBy) {
		return api().get(Offer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

//  **************
//	*   DEALER   *
//	**************
	
	public Api getDealerList(Api.ListListener<Dealer> listener) {
		return getDealerList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerList(Api.ListListener<Dealer> listener, int offset, int limit) {
		return api().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, null));
	}

	public Api getDealerList(Api.ListListener<Dealer> listener, int offset, int limit, String orderBy) {
		return api().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getDealerFromId(Api.ItemListener<Dealer> listener, String dealerId) {
		return api().get(Dealer.ENDPOINT_ID, listener, new Bundle()).setId(dealerId);
	}
	
	public Api getDealerFromIds(Api.ListListener<Dealer> listener, Set<String> dealerIds) {
		return api().get(Dealer.ENDPOINT_LIST, listener, new Bundle()).setDealerIds(dealerIds);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query) {
		return searchDealers(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query, int offset, int limit) {
		return searchDealers(listener, query, offset, limit, null);
	}

	public Api searchDealers(Api.ListListener<Dealer> listener, String query, int offset, int limit, String orderBy) {
		return api().get(Dealer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

//  **************
//	*   STORES   *
//	**************
	
	public Api getStoreList(Api.ListListener<Store> listener) {
		return getStoreList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreList(Api.ListListener<Store> listener, int offset, int limit) {
		return api().get(Store.ENDPOINT_LIST, listener).setOffset(offset).setLimit(limit);
	}

	public Api getStoreList(Api.ListListener<Store> listener, int offset, int limit, String orderBy) {
		return api().get(Store.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getStoreFromId(Api.ItemListener<Store> listener, String storeId) {
		return api().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(storeId);
	}
	
	public Api getStoreFromIds(Api.ListListener<Store> listener, Set<String> storeIds) {
		return api().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(storeIds);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query) {
		return searchStores(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query, int offset, int limit) {
		return searchStores(listener, query, offset, limit, null);
	}

	public Api searchStores(Api.ListListener<Store> listener, String query, int offset, int limit, String orderBy) {
		return api().get(Store.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

}