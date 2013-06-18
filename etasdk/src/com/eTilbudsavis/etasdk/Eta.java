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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Tools.Sort;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Debug tag */
	public static final String TAG = "ETA";
	
	/** 
	 * Variable to decide whether to show debug log messages.<br><br>
	 * Please only set to <code>true</code> while developing to avoid leaking sensitive information */
	public static boolean mDebug = false;
	
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
	private ArrayList<EtaError> mErrors = new ArrayList<EtaError>();
	
	/**
	 * TODO: Write a long story about usage, this will basically be the documentation
	 * @param apiKey
	 *			The API key found at http://etilbudsavis.dk/api/
	 * @Param Context
	 * 			The context of the activity instantiating this class.
	 */
	public Eta(String apiKey, String apiSecret, Context context) {
		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mLocation = new EtaLocation();
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
	 * 
	 * @return Returns the last known session
	 */
	public Session getSession() {
		return mSession;
	}

	/**
	 * 
	 * @return Returns the last known session
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
	 * TODO: Write JavaDoc
	 * @return 
	 */
	public EtaCache getCache() {
		return mCache;
	}
	
	public ShoppinglistManager getShoppinglistManager() {
		return mShoppinglistManager;
	}
	
	/**
	 * Gets a complete list of errors that have occurred<br><br>
	 * 
	 * 
	 * The error log is useful in development for multiple consecutive calls,<br>
	 * that contains errors, where a Log.d() on each error would would flood LogCat.
	 * @return an ArrayList of EtaErrors
	 */
	public ArrayList<EtaError> getErrors(){
		return mErrors;
	}
	
	/**
	 * Add an EtaError to the error log.<br><br>
	 * 
	 * The error log is useful in development for multiple consecutive calls,<br>
	 * that contains errors, where a Log.d() on each error would would flood LogCat.
	 * @param error to add to error list
	 */
	public Eta addError(EtaError error) {
		mErrors.add(error);
		return this;
	}
	
	public Handler getHandler() {
		return mHandler;
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

	public boolean debug() {
		return mDebug;
	}

	public Eta debug(boolean useDebug) {
		mDebug = useDebug;
		return this;
	}
	
	public void onPause() {
		mShoppinglistManager.stopSync();
		mShoppinglistManager.closeDB();
	}
	
	public void onResume() {
		mShoppinglistManager.openDB();
		mShoppinglistManager.startSync();
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

	public Api getCatalogId(Api.CallbackCatalog listener, String id) {
		return api().get(Catalog.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}

	public Api getCatalogId(Api.CallbackCatalog listener, Catalog c) {
		if (c != null) {
			return api().get(Catalog.ENDPOINT_ID, listener, new Bundle()).setId(c.getId());			
		} else {
			return null;
		}
	}

	public Api getCatalogIds(Api.CallbackCatalogList listener, String[] ids) {
		return api().get(Catalog.ENDPOINT_LIST, listener, new Bundle()).setCatalogIds(ids);
	}

	public Api getCatalogIds(Api.CallbackCatalogList listener, List<Catalog> catalogs) {
		if (catalogs != null) {
			String[] ids = new String[catalogs.size()];
			for (int i = 0; i < catalogs.size(); i++) {
				ids[i] = catalogs.get(i).getId();
			}
			return api().get(Catalog.ENDPOINT_LIST, listener, new Bundle()).setCatalogIds(ids);
		} else {
			return null;
		}
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
	
	public Api getOfferIds(Api.CallbackOfferList listener, String[] ids) {
		return api().get(Offer.ENDPOINT_LIST, listener, new Bundle()).setOfferIds(ids);
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
	
	public Api getDealerIds(Api.CallbackDealerList listener, String[] ids) {
		return api().get(Dealer.ENDPOINT_LIST, listener, new Bundle()).setDealerIds(ids);
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
	
	public Api getStoreId(Api.CallbackStore listener, String id) {
		return api().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getStoreIds(Api.CallbackStoreList listener, String[] ids) {
		return api().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(ids);
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