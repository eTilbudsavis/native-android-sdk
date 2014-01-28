package com.eTilbudsavis.etasdk.NetworkHelpers;

import org.json.JSONException;
import org.json.JSONObject;

public class EtaError extends Exception {
	
	private static final long serialVersionUID = 1L;
	
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DETAILS = "details";
    public static final String FAILED_ON_FIELD = "failed_on_field";
    
	public static final String TAG = "EtaError";
	
    private final String mId;
    private final int mCode;
    private final String mMessage;
    private final String mDetails;
    private final String mFailedOnField;

    public EtaError() {
    	this(Code.SDK_UNKNOWN, "Unknown error");
	}
    
    public EtaError(int code, String message) {
    	this(code, message, "None", "None", null);
	}
    
    public EtaError(int code, String message, String id, String details, String failedOnField) {
    	super();
    	mCode = code;
    	mId = id;
    	mMessage = message;
    	mDetails = details;
    	mFailedOnField = failedOnField;
	}
    
    public EtaError(Throwable t, int code, String message) {
    	this(t, code, message, "None", "None", null);
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
			
			String id = apiError.getString(ID);
			int code = apiError.getInt(CODE);
			String message = apiError.getString(MESSAGE);
			String details = apiError.getString(DETAILS);
			String failedOnField = apiError.has("FAILED_ON_FIELD") ? apiError.getString("FAILED_ON_FIELD") : null;
            return new ApiError(code, message, id, details, failedOnField);
            
		} catch (JSONException e) {
			return new ParseError(e);
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
		
	    public static final int SDK_UNKNOWN					= 10000;
	    public static final int SDK_PARSE					= 10100;
	    public static final int SDK_NETWORK					= 10200;
	    public static final int SDK_SESSION					= 10200;
	    
	    public static final int API_SESSION_ERROR			= 1100;
	    public static final int API_TOKEN_EXPIRED			= 1101;
	    public static final int API_INVALID_API_KEY			= 1102;
	    public static final int API_MISSING_SIGNATURE		= 1103;
	    public static final int API_INVALID_SIGNATURE		= 1104;
	    public static final int API_TOKEN_NOT_ALLOWED		= 1105;
	    public static final int API_MISSING_ORIGIN_HEADER	= 1106;
	    public static final int API_MISSING_TOKEN			= 1107;
	    public static final int API_INVALID_TOKEN			= 1108;
	    
	}
	
}
