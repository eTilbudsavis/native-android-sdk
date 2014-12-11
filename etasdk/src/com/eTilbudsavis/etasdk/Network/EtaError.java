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
package com.eTilbudsavis.etasdk.Network;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Impl.ApiError;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.Network.Impl.ParseError;

public class EtaError extends Exception {

	public static final String TAG = Eta.TAG_PREFIX + EtaError.class.getSimpleName();
	
	private static final long serialVersionUID = 1L;

    protected static final String ID = "id";
    protected static final String CODE = "code";
    protected static final String MESSAGE = "message";
    protected static final String DETAILS = "details";
    protected static final String FAILED_ON_FIELD = "failed_on_field";
    
    private final String mId;
    private final int mCode;
    private final String mMessage;
    private final String mDetails;
    private final String mFailedOnField;
    
    public EtaError() {
    	this(Code.UNKNOWN, "Unknown error","Unknown error, there is no "
    			+ "information available. Please contact support.");
	}
    
    public EtaError(int code, String message, String details) {
    	this(code, message, "null", details, null);
	}
    
    public EtaError(int code, String message, String id, String details, String failedOnField) {
    	super();
    	mCode = code;
    	mId = id;
    	mMessage = message;
    	mDetails = details;
    	mFailedOnField = failedOnField;
	}
    
    public EtaError(Throwable t, int code, String message, String details) {
    	this(t, code, message, "null", details, null);
	}
    
    public EtaError(Throwable t, int code, String message, String id, String details, String failedOnField) {
    	super(t);
    	mCode = code;
    	mId = id;
    	mMessage = message;
    	mDetails = details;
    	mFailedOnField = failedOnField;
	}
    
    /**
     * Method will return an ApiError if the provided JSONObject is an API error.<br>
     * Otherwise it will return a ParseError.
     * @param apiError
     * @return
     */
    public static EtaError fromJSON(JSONObject apiError) {
    	
		try {
			
			// Not using Json-class to parse data, as we'd rather want it to fail
			String id = apiError.getString(ID);
			int code = apiError.getInt(CODE);
			String message = apiError.getString(MESSAGE);
			String details = apiError.getString(DETAILS);
			String failedOnField = apiError.has(FAILED_ON_FIELD) ? apiError.getString(FAILED_ON_FIELD) : null;
            return new ApiError(code, message, id, details, failedOnField);
            
		} catch (Exception e) {
			EtaLog.e(TAG, "", e);
			return new ParseError(e, ApiError.class);
		}
		
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
		JSONObject e = new JSONObject();
		try {
			e.put(ID, mId);
			e.put(CODE, mCode);
			e.put(MESSAGE, mMessage);
			e.put(DETAILS, mDetails);
			e.put(FAILED_ON_FIELD, mFailedOnField);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return e;
	}
	
	public boolean isSdk() {
		int code = getCode();
		return 10000 <= code && code < 11000;
	}
	
	public boolean isApi() {
		int code = getCode();
		return 1000 <= code && code < 10000;
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

		/**
		 * Auto loading of objects failed
		 */
	    public static final int AUTO_LOAD_ERROR				= 10300;

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

	    /** This call requires a request location. See documentation. */
	    public static final int MISSING_LOCATION			= 1401;

	    /** This call requires a request radius. See documentation. */
	    public static final int MISSING_RADIUS				= 1402;

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
	    
	    
	}
	
}
