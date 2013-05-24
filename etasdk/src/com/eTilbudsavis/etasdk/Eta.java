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

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Sort;
import Utils.Utilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.RequestListener;
import com.eTilbudsavis.etasdk.Api.RequestType;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Session;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "ETA";
	
	/** Debug log messages only while developing */
	public static final boolean DEBUG = true;
	
	/** Info log messages may be used in release */
	public static final boolean DEBUG_I = true;
	
	private static Context mContext;
	private SharedPreferences prefs;
	
	public static final String PREFS_NAME = "eta_sdk";
	public static final String PREFS_SESSION = "session";
	
	// Authorization.
	private final String mApiKey;
	private final String mApiSecret;
	private Session mSession = new Session();
	
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
		mContext = context;
		
		mLocation = new EtaLocation();
		mCache = new EtaCache();

		prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

		prefs.edit().clear().commit();
		
		String sessionJson = prefs.getString(PREFS_SESSION, null);
		if (sessionJson != null) {
			try {
				mSession.update(new JSONObject(sessionJson));
				if (mSession.getExpire() < System.currentTimeMillis())
					updateSession(null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			updateSession(null);
		}
	}
	
	public void updateSession(final RequestListener listener) {
		
		Utilities.logi(TAG, "Session not established. Trying to establish, please wait...");
		Api a = new Api(this);
		a.setUseLocation(false);
		a.build(Session.ENDPOINT, new RequestListener() {
			
			public void onComplete(int responseCode, Object object) {
				
				if (responseCode == 201) {
					try {
						mSession.update(new JSONObject(object.toString()));
						prefs.edit().putString(PREFS_SESSION, mSession.getJson()).commit();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Utilities.logd(TAG, "Session error: " + String.valueOf(responseCode) + " - " + object.toString());
				}
				
				if (listener != null) {
					listener.onComplete(responseCode, object);
				}
				mSession.notifySubscribers();
			}
		}, Api.RequestType.POST);
		a.execute();
		
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
	 * TODO: Write JavaDoc
	 * Useful when multiple consecutive calls, that contains errors.
	 * Then a Log.d() on each call will flood LogCat.
	 * @return
	 */
	public ArrayList<EtaError> getErrors(){
		return mErrors;
	}
	
	/**
	 * TODO: Write JavaDoc
	 * @param error
	 */
	public void addError(EtaError error) {
		mErrors.add(error);
	}
	
	// TODO: Need a lot of wrapper methods here, they all must call API
	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset) {
		return getCatalogs(listener, offset, Api.LIMIT_DEFAULT);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, String[] order) {
		return getCatalogs(listener, offset, Api.LIMIT_DEFAULT, order);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, int limit) {
		return getCatalogs(listener, offset, limit, null);
	}

	public HttpHelper getCatalogs(Api.CatalogListListener listener, int offset, int limit, String[] order) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Api.OFFSET, offset);
		apiParams.putInt(Api.LIMIT, limit);
		if (order != null)
			apiParams.putStringArray(Sort.ORDER_BY, order);
		Api a = new Api(this);
		a.build(Endpoint.CATALOG_LIST, listener, apiParams, Api.RequestType.GET);
		return a.execute();
	}
	
}