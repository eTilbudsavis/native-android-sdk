package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class EtaError extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaError";

	public static final int SDK_ERROR_UNKNOWN			= 10000;
	public static final int SDK_ERROR_MISMATCH			= 10001;
	
	public static final int UNKNOWN_HOST				= 10100;
	public static final int CLIENT_PROTOCOL_EXCEPTION	= 10101;
	public static final int IO_EXCEPTION				= 10102;
	
	private String mId = new String();
	private int mCode = 0;
	private String mMessage = new String();
	private String mDetails = new String();
	private String mOrigData = new String();
	private Date mTime = null;
	
	public EtaError() {
		mTime = new Date();
	}
	
	@SuppressWarnings("unchecked")
	public static EtaError fromJSON(JSONObject error) {
		return fromJSON(new EtaError(), error);
	}
	
	private static EtaError fromJSON(EtaError er, JSONObject error) {
		if (er == null) er = new EtaError();
		if (error == null) return er;
		
		try {
			er.setId(getJsonString(error, S_ID));
			er.setCode(error.getInt(S_CODE));
			er.setMessage(getJsonString(error, S_MESSAGE));
			er.setDetails(getJsonString(error, S_DETAILS));
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return er;
	}

	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(EtaError er) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_ID, er.getId());
			o.put(S_CODE, er.getCode());
			o.put(S_MESSAGE, er.getMessage());
			o.put(S_DETAILS, er.getDetails());
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return o;
	}
	
	public EtaError setId(String id) {
		mId = id;
		return this;
	}

	public EtaError setCode(int code) {
		mCode = code;
		return this;
	}

	public EtaError setMessage(String message) {
		mMessage = message;
		return this;
	}

	public EtaError setDetails(String details) {
		mDetails = details;
		return this;
	}

	public EtaError setTime(Date time) {
		mTime = time;
		return this;
	}

	public String getId() {
		return mId;
	}

	public int getCode() {
		return mCode;
	}

	public String getMessage() {
		return mMessage;
	}

	public String getDetails() {
		return mDetails;
	}

	public String getOriginalData() {
		return mOrigData;
	}

	public EtaError setOriginalData(String data) {
		mOrigData = data;
		return this;
	}
	
	public Date getTime() {
		return mTime;
	}

	/**
	 * Gives a detailed description of this object.<br>
	 * E.g.: <code>{ id: 123xyz, code: 123xyz, message: the message, details: the details, time: 1369211987830 }</code> 
	 * @return a printable representation of this object
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.append(getClass().getSimpleName()).append("[")
				.append("id=").append(mId)
				.append(", code=").append(mCode)
				.append(", message=").append(mMessage)
				.append(", details=").append(mDetails)
				.append(", time=").append(Utils.formatDate(getTime()))
				.append(", original_data=").append(mOrigData)
				.append("]").toString();
	}
}
