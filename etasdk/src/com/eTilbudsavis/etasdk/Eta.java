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
	
	// TODO: Need a lot of wrapper methods here, they all must call API
	/**
	 * @param listener
	 * @param offset
	 * @return
	 * @see com.eTilbudsavis.etasdk.Api.CatalogListListener #onComplete(int, Object)
	 */
	public HttpHelper getCatalogList(Api.CatalogListListener listener, int offset) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, null);
	}
	
	public HttpHelper getCatalogList(Api.CatalogListListener listener, int offset, String[] orderBy) {
		return getCatalogList(listener, offset, Api.DEFAULT_LIMIT, orderBy);
	}

	public HttpHelper getCatalogList(Api.CatalogListListener listener, int offset, int limit) {
		return getCatalogList(listener, offset, limit, null);
	}

	public HttpHelper getCatalogList(Api.CatalogListListener listener, int offset, int limit, String[] orderBy) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Catalog.PARAM_OFFSET, offset);
		apiParams.putInt(Catalog.PARAM_LIMIT, limit);
		if (orderBy != null)
			apiParams.putString(Sort.ORDER_BY, TextUtils.join(",", orderBy));
		return api().get(Catalog.ENDPOINT_LIST, listener, apiParams).execute();
	}

	public HttpHelper getCatalogId(Api.CatalogListener listener, String id) {
		return api().get(Catalog.ENDPOINT_ID, listener, new Bundle()).setId(id).execute();
	}

	public HttpHelper getCatalogIds(Api.CatalogListListener listener, String[] ids) {
		return api().get(Catalog.ENDPOINT_LIST, listener, new Bundle()).setCatalogIds(ids).execute();
	}
	
	public HttpHelper getStoreList(Api.StoreListListener listener) {
		return getStoreList(listener, null);
	}
	
	public HttpHelper getStoreList(Api.StoreListListener listener, String[] orderBy) {
		Bundle apiParams = new Bundle();
		if (orderBy != null)
			apiParams.putString(Sort.ORDER_BY, TextUtils.join(",", orderBy));
		return api().get(Store.ENDPOINT_LIST, listener, apiParams).execute();
	}

	public HttpHelper getStoreId(Api.StoreListener listener, String id) {
		return api().get(Store.ENDPOINT_ID, listener, new Bundle()).setId(id).execute();
	}

	public HttpHelper getStoreIds(Api.StoreListListener listener, String[] ids) {
		return api().get(Store.ENDPOINT_LIST, listener, new Bundle()).setStoreIds(ids).execute();
	}
	
	
	
}