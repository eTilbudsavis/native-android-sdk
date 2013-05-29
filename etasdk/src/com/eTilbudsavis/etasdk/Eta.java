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
import Utils.Sort;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Session;

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
		mLocation = new EtaLocation();
		mCache = new EtaCache();
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset) {
		return getCatalogs(listener, offset, Api.DEFAULT_LIMIT);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, String[] order) {
		return getCatalogs(listener, offset, Api.DEFAULT_LIMIT, order);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, int limit) {
		return getCatalogs(listener, offset, limit, null);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, int limit, String[] order) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Api.OFFSET, offset);
		apiParams.putInt(Api.LIMIT, limit);
		if (order != null)
			apiParams.putString(Sort.ORDER_BY, TextUtils.join(",", order));
		return api().get(Endpoint.CATALOG_LIST, listener, apiParams).execute();
	}
	
}