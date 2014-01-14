package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class EtaError extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "EtaError";
	
    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";
    
    private JSONObject mApiData;
    
    private String mId;
    private int mCode = 0;
    private String mMessage = new String();
    private String mDetails = new String();
    private String mFailedOnField = new String();
    
	public EtaError() { }
	
	public EtaError(int code, String message) {
		mCode = code;
		mMessage = message;
	}
	
	public EtaError(Request<?> request, NetworkResponse response) {
		
		try {
			String data;
			try {
				data = new String(response.data, request.getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				data = new String(response.data);
			}
			mApiData = new JSONObject(data);
			mId = mApiData.getString(ID);
			mCode = mApiData.getInt(CODE);
			mMessage = mApiData.getString(MESSAGE);
			mDetails = mApiData.getString(DETAILS);
            if (mApiData.has("failed_on_field")) {
                mFailedOnField = mApiData.getString("failed_on_field");
            }
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			mCode = Sdk.UNKNOWN;
			mMessage = "Unknown error parsing network body data";
		}
		
	}
	
	public EtaError(String detailMessage) {
		super(detailMessage);
	}
	
	public EtaError(Throwable t) {
		super(t);
	}

	public EtaError(String detailMessage, Throwable t) {
		super(detailMessage, t);
	}
	
    public EtaError setId(String id) {
            mId = id;
            return this;
    }
    
    public EtaError setCode(int code) {
            mCode = code;
            return this;
    }
    
    public EtaError setDetails(String details) {
            mDetails = details;
            return this;
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
    
    public void setFailedOnField(String failedOnField) {
            mFailedOnField = failedOnField;
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
	
	public class Sdk {
		
	    public static final int UNKNOWN						= 10000;
	    public static final int MISMATCH					= 10001;
	    public static final int UNKNOWN_HOST				= 10100;
	    public static final int CLIENT_PROTOCOL_EXCEPTION	= 10101;
	    public static final int IO_EXCEPTION				= 10102;
	    
	}
	
	public class Api {
		
	    public static final int SESSION_ERROR			= 1100;
	    public static final int TOKEN_EXPIRED			= 1101;
	    public static final int INVALID_API_KEY			= 1102;
	    public static final int MISSING_SIGNATURE		= 1103;
	    public static final int INVALID_SIGNATURE		= 1104;
	    public static final int TOKEN_NOT_ALLOWED		= 1105;
	    public static final int MISSING_ORIGIN_HEADER	= 1106;
	    public static final int MISSING_TOKEN			= 1107;
	    public static final int INVALID_TOKEN			= 1108;
	    
	}
	
}
