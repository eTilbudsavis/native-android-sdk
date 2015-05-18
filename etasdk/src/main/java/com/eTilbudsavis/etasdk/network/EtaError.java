/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.network;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.network.impl.ApiError;
import com.eTilbudsavis.etasdk.network.impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class EtaError extends Exception implements IJson<JSONObject> {

	public static final String TAG = Constants.getTag(EtaError.class);
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEF_MESSAGE = "Unknown error";
	private static final String DEF_DETAILS = "Unknown error. No information available. Please contact support.";
	
    private final String mId;
    private final int mCode;
    private final String mDetails;
    private final String mFailedOnField;

    public EtaError(Throwable t, int code, String message, String id, String details, String failedOnField) {
    	super(message, t);
    	mCode = code;
    	mId = id;
    	mDetails = details;
    	mFailedOnField = failedOnField;
	}
    
    public EtaError(int code, String message, String id, String details, String failedOnField) {
    	this(null, code, message, id, details, failedOnField);
	}
    
    public EtaError() {
    	this(null, Code.UNKNOWN, DEF_MESSAGE, null, DEF_DETAILS, null);
	}
    
    public EtaError(int code, String message, String details) {
    	this(null, code, message, null, details, null);
	}
    
    public EtaError(Throwable t, int code, String message, String details) {
    	this(t, code, message, null, details, null);
    	
	}
    
    /**
     * Method will return an ApiError if the provided JSONObject is an API error.<br>
     * Otherwise it will return a ParseError.
     * @param apiError
     * @return
     */
    public static EtaError fromJSON(JSONObject apiError) {
    	
    	String id = Json.valueOf(apiError, JsonKey.ID);
		int code = Json.valueOf(apiError, JsonKey.CODE, Code.UNKNOWN);
		String message = Json.valueOf(apiError, JsonKey.MESSAGE, DEF_MESSAGE);
		String details = Json.valueOf(apiError, JsonKey.DETAILS, DEF_DETAILS);
		String failedOnField = Json.valueOf(apiError, JsonKey.FAILED_ON_FIELD);
        return new ApiError(code, message, id, details, failedOnField);
        
    }
    
    /**
     * Id is the unique reference to this specific error on the API.<br>
     * You can write to support, to get specific info about this error.<br>
     * NOTE: If id is null, this is a SDK error. Therefore there is no logging of the error.
     * @return
     */
    public String getId() {
    	return mId;
    }
    
    public int getCode() {
    	return mCode;
    }
    
    public String getDetails() {
    	return mDetails;
    }
    
    public String getFailedOnField() {
    	return mFailedOnField;
    }
    
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.ID, Json.nullCheck(mId));
			o.put(JsonKey.CODE, Json.nullCheck(mCode));
			o.put(JsonKey.MESSAGE, Json.nullCheck(getMessage()));
			o.put(JsonKey.DETAILS, Json.nullCheck(mDetails));
			o.put(JsonKey.FAILED_ON_FIELD, Json.nullCheck(mFailedOnField));
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return o;
	}
	
	public boolean isSdk() {
		int code = getCode();
		return 10000 <= code && code < 11000;
	}
	
	public boolean isApi() {
		int code = getCode();
		return 1000 <= code && code < 10000;
	}
	
	/**
	 * Prints the JSON representation of this object
	 */
	@Override
	public String toString() {
		return toJSON().toString();
	}
	
	public class Code {
		
		/** The error could not be identified */
	    public static final int UNKNOWN						= 10000;

		/** 
		 * There was an error trying to parse the data from the API. If it's
		 * JSON data, check the keys, and that the endpoint-return-format,
		 * matches the {@link Request} type, e.g. when using
		 * {@link JsonObjectRequest} it is a requirement that the endpoint
		 * returns valid JSOBObject-data.
		 */
	    public static final int PARSE_ERROR					= 10100;

		/**
		 * There was an error establishing a connection to the API. Please check
		 * that the device has a working internet connection.
		 */
	    public static final int NETWORK_ERROR				= 10200;

		/** Auto loading of objects failed */
		public static final int AUTO_LOAD_ERROR				= 10300;

		/** Pageflip failed, reason unknown */
		public static final int PAGEFLIP					= 10400;

		/** Loading data for the catalog to display failed */
		public static final int PAGEFLIP_CATALOG_LOADING_FAILED	= 10401;

		/** Loading of pages for pageflip to display failed */
		public static final int PAGEFLIP_LOADING_PAGES_FAILED	= 10402;

		/**
		 * Out of memory error occurred, no further work possible
		 */
	    public static final int OUT_OF_MEMORY				= 10400;
	    
		/** Session error */
	    public static final int SESSION_ERROR				= 1100;
	    
		/** You must create a new one to continue. */
	    public static final int TOKEN_EXPIRED				= 1101;
	    
		/** Could not find app matching your api key. */
	    public static final int INVALID_API_KEY				= 1102;
	    
		/** Only webpages are allowed to rely on domain name
		 * matching. Your request did not send the HTTP_HOST header, so you
		 * would have to supply a signature. See docs.
		 */
	    public static final int MISSING_SIGNATURE			= 1103;
	    
		/** Signature given but did not match. */
	    public static final int INVALID_SIGNATURE			= 1104;
	    
		/** This token can not be used with this app. Ensure correct domain
		 * rules in app settings.
		 */
	    public static final int TOKEN_NOT_ALLOWED			= 1105;
	    
		/** This token can not be used without a valid Origin header. */
	    public static final int MISSING_ORIGIN_HEADER		= 1106;
	    
		/** No token found in request to an endpoint that requires a valid token. */
	    public static final int MISSING_TOKEN				= 1107;
	    
		/** Token is not valid. */
	    public static final int INVALID_TOKEN				= 1108;
	    
	    /** Authentication error */
	    public static final int AUTENTICATION_ERROR			= 1200;

	    /** Did you supply the correct user credentials? */
	    public static final int USER_AUTENTICATION_FAILED	= 1201;

	    /** User not verified. */
	    public static final int USER_NOT_VERIFIED			= 1202;

	    /** Authorization error. */
	    public static final int AUTHORIZATION_ERROR			= 1300;

	    /** Action not allowed in within current session (permission error) */
	    public static final int PERMISSION_ERROR			= 1301;

	    /** Request invalid due to missing information. */
	    public static final int MISSING_INFORMATION			= 1400;

	    /** This endpoint requires a request location. See documentation. */
	    public static final int MISSING_LOCATION			= 1401;

	    /** This endpoint requires a request radius. See documentation. */
	    public static final int MISSING_RADIUS				= 1402;

	    /** the email field is missing from Facebook user data. */
	    public static final int FACEBOOK_MISSING_EMAIL		= 1431;

	    /** the birthday field is missing from Facebook user data. */
	    public static final int FACEBOOK_MISSING_BIRTHDAY	= 1432;

	    /** the gender field is missing from Facebook user data. */
	    public static final int FACEBOOK_MISSING_GENDER		= 1433;

	    /** the locale field is missing from Facebook user data. */
	    public static final int FACEBOOK_MISSING_LOCALE		= 1434;

	    /** the name field is missing from Facebook user data. */
	    public static final int FACEBOOK_MISSING_NAME		= 1435;
	    
	    /** Invalid information */
	    public static final int INVALID_INFORMATION			= 1500;

	    /** Invalid resource id */
	    public static final int INVALID_RESOURCE_ID			= 1501;

	    /** Dublication of resource */
	    public static final int DUBLICATION_OF_RESOURCE		= 1530;
	    
	    /** 
	     * Ensure body data is of valid syntax, and that you send a correct
	     * Content-Type header
	     */
	    public static final int INVALID_BODY_DATA			= 1566;

	    /** Please contact support with error id. */
	    public static final int INTERNAL_INTEGRITY_ERROR	= 2000;

	    /** Please contact support with error id. */
	    public static final int INTERNAL_SEARCH_ERROR		= 2010;

	    /** System trying to autofix. Please repeat request. */
	    public static final int NON_CRITICAL_INTERNAL_ERROR	= 1201;
	    
	    /** Error message describes problem */
	    public static final int ACTION_DOES_NOT_EXIST		= 4000;
	    
	    /** Service is unavailable. We are working on it. */
	    public static final int SERVICE_UNAVAILABLE			= 5000;
	    
	    /** Service is down for maintenance, The entire service is down, don't send requests */
	    public static final int SERVICE_DOWN_MAINTENANCE	= 5010;
	    
	    /** Feature is down for maintenance. Dont send same request again. */
	    public static final int FEATURE_DOWN_MAINTENANCE	= 5020; 
	    
	}
	
}
