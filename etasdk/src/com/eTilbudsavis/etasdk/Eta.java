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

import Utils.Endpoint;
import Utils.Params;
import Utils.Sort;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.EtaObjects.Store;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Debug tag */
	public static final String TAG = "ETA";
	
	/** 
	 * Variable to decide whether to show debug log messages.<br><br>
	 * Please only set to <code>true</code> while developing to avoid leaking sensitive information */
	public static final boolean DEBUG = true;
	
	/** 
	 * Variable to decide whether to show info log messages.<br><br>
	 * Can be true in a release version without further implications */
	public static final boolean DEBUG_I = true;
	
	/** The date format as returned from the server */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss+SSSS";
	
	/** Name for the SDK SharedPreferences file */
	public static final String PREFS_NAME = "eta_sdk";
	
	private final String mApiKey;
	private final String mApiSecret;
	private Session mSession;
	private SharedPreferences mPrefs;
	private EtaLocation mLocation;
	private EtaCache mCache;
	private ArrayList<EtaError> mErrors = new ArrayList<EtaError>();
	
	/**
	 * TODO: Write a long story about usage, this will basically be the documentation
	 * @param apiKey
	 *			The API key found at http://etilbudsavis.dk/api/
	 * @Param Context
	 * 			The context of the activity instantiating this class.
	 */
	public Eta(String apiKey, String apiSecret, Context context) {
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mLocation = new EtaLocation();
		mCache = new EtaCache();
		mSession = new Session(this);
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
	 * This includes Session, User, Api and other data.
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	public boolean clearPreferences() {
		boolean status = mPrefs.edit().clear().commit();
		mSession = new Session(this);
		return status;
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
	
	public Api getCatalogList(Api.CatalogListListener listener) {
		return getCatalogList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getCatalogList(Api.CatalogListListener listener, int offset) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getCatalogList(Api.CatalogListListener listener, int offset, int limit) {
		return getCatalogList(listener, offset, limit, null);
	}
	
	public Api getCatalogList(Api.CatalogListListener listener, int offset, String[] orderBy) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getCatalogList(Api.CatalogListListener listener, int offset, int limit, String[] orderBy) {
		return api().get(Catalog.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getCatalogId(Api.CatalogListener listener, String id) {
		return api().get(Catalog.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getCatalogIds(Api.CatalogListListener listener, String[] ids) {
		return api().get(Catalog.ENDPOINT_LIST, listener, new Bundle()).setCatalogIds(ids);
	}

	// ######################## OFFERS ########################
	
	public Api getOfferList(Api.OfferListListener listener) {
		return getOfferList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getOfferList(Api.OfferListListener listener, int offset) {
		return getOfferList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferList(Api.OfferListListener listener, int offset, int limit) {
		return getOfferList(listener, offset, limit, null);
	}

	public Api getOfferList(Api.OfferListListener listener, int offset, String[] orderBy) {
		return getOfferList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getOfferList(Api.OfferListListener listener, int offset, int limit, String[] orderBy) {
		return api().get(Offer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getOfferId(Api.OfferListener listener, String id) {
		return api().get(Offer.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getOfferIds(Api.OfferListListener listener, String[] ids) {
		return api().get(Offer.ENDPOINT_LIST, listener, new Bundle()).setOfferIds(ids);
	}
	
	public Api getOfferSearch(Api.OfferListListener listener, String query) {
		return getOfferSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferSearch(Api.OfferListListener listener, String query, int offset) {
		return getOfferSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getOfferSearch(Api.OfferListListener listener, String query, String[] orderBy) {
		return getOfferSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getOfferSearch(Api.OfferListListener listener, String query, int offset, int limit) {
		return getOfferSearch(listener, query, offset, limit, null);
	}

	public Api getOfferSearch(Api.OfferListListener listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Offer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}
	
	// ######################## DEALERS ########################

	public Api getDealerList(Api.DealerListListener listener) {
		return getDealerList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getDealerList(Api.DealerListListener listener, int offset) {
		return getDealerList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerList(Api.DealerListListener listener, int offset, int limit) {
		return getDealerList(listener, offset, limit, null);
	}
	
	public Api getDealerList(Api.DealerListListener listener, int offset, String[] orderBy) {
		return getDealerList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getDealerList(Api.DealerListListener listener, int offset, int limit, String[] orderBy) {
		return api().get(Dealer.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getDealerId(Api.DealerListener listener, String id) {
		return api().get(Dealer.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getDealerIds(Api.DealerListListener listener, String[] ids) {
		return api().get(Dealer.ENDPOINT_LIST, listener, new Bundle()).setDealerIds(ids);
	}

	public Api getDealerSearch(Api.DealerListListener listener, String query) {
		return getDealerSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerSearch(Api.DealerListListener listener, String query, int offset) {
		return getDealerSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getDealerSearch(Api.DealerListListener listener, String query, int offset, int limit) {
		return getDealerSearch(listener, query, offset, limit, null);
	}

	public Api getDealerSearch(Api.DealerListListener listener, String query, int offset, String[] orderBy) {
		return getDealerSearch(listener, query, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getDealerSearch(Api.DealerListListener listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Dealer.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}


	// ######################## STORES ########################

	public Api getStoreList(Api.StoreListListener listener) {
		return getStoreList(listener, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}
	
	public Api getStoreList(Api.StoreListListener listener, int offset) {
		return getStoreList(listener, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreList(Api.StoreListListener listener, int offset, int limit) {
		return getStoreList(listener, offset, limit, null);
	}
	
	public Api getStoreList(Api.StoreListListener listener, int offset, String[] orderBy) {
		return getStoreList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}
	
	public Api getStoreList(Api.StoreListListener listener, int offset, int limit, String[] orderBy) {
		return api().get(Store.ENDPOINT_LIST, listener, getApiParams(offset, limit, orderBy));
	}
	
	public Api getStoreId(Api.StoreListener listener, String id) {
		return api().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(id);
	}
	
	public Api getStoreIds(Api.StoreListListener listener, String[] ids) {
		return api().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(ids);
	}

	public Api getStoreSearch(Api.StoreListListener listener, String query) {
		return getStoreSearch(listener, query, Api.DEFAULT_OFFSET, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreSearch(Api.StoreListListener listener, String query, int offset) {
		return getStoreSearch(listener, query, offset, Api.DEFAULT_LIMIT, null);
	}

	public Api getStoreSearch(Api.StoreListListener listener, String query, int offset, int limit) {
		return getStoreSearch(listener, query, offset, limit, null);
	}

	public Api getStoreSearch(Api.StoreListListener listener, String query, int offset, String[] orderBy) {
		return getStoreSearch(listener, query, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public Api getStoreSearch(Api.StoreListListener listener, String query, int offset, int limit, String[] orderBy) {
		return api().get(Store.ENDPOINT_SEARCH, listener, getSearchApiParams(offset, limit, orderBy, query));
	}

}