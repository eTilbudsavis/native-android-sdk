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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaLocation.LocationListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.User;
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
	private ExecutorService mThreads = Executors.newFixedThreadPool(5);
	
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
	
	private Bundle getApiParams(int offset, int limit, String[] orderBy) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Catalog.PARAM_OFFSET, offset);
		apiParams.putInt(Catalog.PARAM_LIMIT, limit);
		if (orderBy != null)
			apiParams.putString(Sort.ORDER_BY, TextUtils.join(",", orderBy));
		return apiParams;
	}
	
	private Bundle getSearchApiParams(int offset, int limit, String[] orderBy, String query) {
		Bundle apiParams = getApiParams(offset, limit, orderBy);
		apiParams.putString(Catalog.PARAM_QUERY, query);
		return apiParams;
	}

	
// ######################## CATALOGS ########################
	
	public Api getCatalogList(Api.CallbackCatalogList listener) {
		return getCatalogList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getCatalogList(Api.CallbackCatalogList listener, int offset) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getCatalogList(Api.CallbackCatalogList listener, int offset, int limit) {
		return getCatalogList(listener, offset, limit, null);
	}
	
	public Api getCatalogList(Api.CallbackCatalogList listener, int offset, String[] orderBy) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getCatalogList(Api.CallbackCatalogList listener, int offset, int limit, String[] orderBy) {
		return api().get(Catalog.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}

	public Api getCatalogId(Api.CallbackCatalog listener, String catalogId) {
		return api().get(Catalog.ENDPOINT_ID, listener, new Bundle()).setId(catalogId);
	}

	public Api getCatalogIds(Api.CallbackCatalogList listener, List<String> catalogIds) {
		return api().get(Catalog.ENDPOINT_LIST, listener, new Bundle()).setCatalogIds(catalogIds);
	}

// ######################## OFFERS ########################
	
	public Api getOfferList(Api.CallbackOfferList listener) {
		return getOfferList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getOfferList(Api.CallbackOfferList listener, int offset) {
		return getOfferList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferList(Api.CallbackOfferList listener, int offset, int limit) {
		return getOfferList(listener, offset, limit, null);
	}

	public Api getOfferList(Api.CallbackOfferList listener, int offset, String[] orderBy) {
		return getOfferList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getOfferList(Api.CallbackOfferList listener, int offset, int limit, String[] orderBy) {
		return api().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getOfferId(Api.CallbackOffer listener, String id) {
		return api().get(Offer.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getOfferIds(Api.CallbackOfferList listener, List<String> offerIds) {
		return api().get(Offer.ENDPOINT_LIST, listener, new Bundle()).setOfferIds(offerIds);
	}
	
	public Api getOfferSearch(Api.CallbackOfferList listener, String query) {
		return getOfferSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferSearch(Api.CallbackOfferList listener, String query, int offset) {
		return getOfferSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferSearch(Api.CallbackOfferList listener, String query, String[] orderBy) {
		return getOfferSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getOfferSearch(Api.CallbackOfferList listener, String query, int offset, int limit) {
		return getOfferSearch(listener, query, offset, limit, null);
	}

	public Api getOfferSearch(Api.CallbackOfferList listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Offer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}
	
// ######################## DEALERS ########################

	public Api getDealerList(Api.CallbackDealerList listener) {
		return getDealerList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getDealerList(Api.CallbackDealerList listener, int offset) {
		return getDealerList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerList(Api.CallbackDealerList listener, int offset, int limit) {
		return getDealerList(listener, offset, limit, null);
	}
	
	public Api getDealerList(Api.CallbackDealerList listener, int offset, String[] orderBy) {
		return getDealerList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getDealerList(Api.CallbackDealerList listener, int offset, int limit, String[] orderBy) {
		return api().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getDealerId(Api.CallbackDealer listener, String id) {
		return api().get(Dealer.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getDealerIds(Api.CallbackDealerList listener, List<String> dealerIds) {
		return api().get(Dealer.ENDPOINT_LIST, listener, new Bundle()).setDealerIds(dealerIds);
	}

	public Api getDealerSearch(Api.CallbackDealerList listener, String query) {
		return getDealerSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerSearch(Api.CallbackDealerList listener, String query, int offset) {
		return getDealerSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerSearch(Api.CallbackDealerList listener, String query, int offset, int limit) {
		return getDealerSearch(listener, query, offset, limit, null);
	}

	public Api getDealerSearch(Api.CallbackDealerList listener, String query, int offset, String[] orderBy) {
		return getDealerSearch(listener, query, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getDealerSearch(Api.CallbackDealerList listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Dealer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

// ######################## STORES ########################

	public Api getStoreList(Api.CallbackStoreList listener) {
		return getStoreList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getStoreList(Api.CallbackStoreList listener, int offset) {
		return getStoreList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreList(Api.CallbackStoreList listener, int offset, int limit) {
		return getStoreList(listener, offset, limit, null);
	}
	
	public Api getStoreList(Api.CallbackStoreList listener, int offset, String[] orderBy) {
		return getStoreList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getStoreList(Api.CallbackStoreList listener, int offset, int limit, String[] orderBy) {
		return api().get(Store.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getStoreFromId(Api.CallbackStore listener, String id) {
		return api().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getStoreFromIds(Api.CallbackStoreList listener, List<String> storeIds) {
		return api().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(storeIds);
	}

	public Api getStoreSearch(Api.CallbackStoreList listener, String query) {
		return getStoreSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreSearch(Api.CallbackStoreList listener, String query, int offset) {
		return getStoreSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreSearch(Api.CallbackStoreList listener, String query, int offset, int limit) {
		return getStoreSearch(listener, query, offset, limit, null);
	}

	public Api getStoreSearch(Api.CallbackStoreList listener, String query, int offset, String[] orderBy) {
		return getStoreSearch(listener, query, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getStoreSearch(Api.CallbackStoreList listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Store.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

}