package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.Eta;

public class EtaError implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaError";

	private static final String S_ID = "id";
	private static final String S_CODE = "code";
	private static final String S_MESSAGE = "message";
	private static final String S_DETAILS = "details";
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);
	
	private String mId = new String();
	private int mCode = 0;
	private String mMessage = new String();
	private String mDetails = new String();
	private String mOrigData = new String();
	private long mTime = 0L;
	
	public EtaError() {
		
	}
	
	public static EtaError fromJSON(String error) {
		EtaError er = new EtaError();
		try {
			er = fromJSON(er, new JSONObject(error));
		} catch (JSONException e) {
			er.setOriginalData(error);
		}
		return er;
	}
	
	public static EtaError fromJSON(JSONObject error) {
		return fromJSON(new EtaError(), error);
	}
	
	private static EtaError fromJSON(EtaError er, JSONObject error) {
		if (er == null) er = new EtaError();
		if (error == null) return er;
		
		try {
			er.setId(error.getString(S_ID));
			er.setCode(error.getInt(S_CODE));
			er.setMessage(error.getString(S_MESSAGE));
			er.setDetails(error.getString(S_DETAILS));
			er.setTime(System.currentTimeMillis());
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return o;
	}
	
	public void setId(String id) {
		mId = id;
	}

	public void setCode(int code) {
		mCode = code;
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public void setDetails(String details) {
		mDetails = details;
	}

	public void setTime(long time) {
		mTime = time;
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
	
	public long getTime() {
		return mTime;
	}

	public String getTimeString() {
		return sdf.format(new Date(mTime));
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
				.append(", time=").append(getTimeString())
				.append(", original_data=").append(mOrigData)
				.append("]").toString();
	}
}
