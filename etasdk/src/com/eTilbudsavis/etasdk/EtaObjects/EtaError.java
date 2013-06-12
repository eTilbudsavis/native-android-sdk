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

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);
	
	private String mId = new String();
	private int mCode = 0;
	private String mMessage = new String();
	private String mDetails = new String();
	private long mTime = 0L;
	
	public EtaError() {
		
	}
	
	public static EtaError fromJSON(String error) {
		try {
			return fromJSON(new EtaError(), new JSONObject(error));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static EtaError fromJSON(JSONObject error) {
		return fromJSON(new EtaError(), error);
	}
	
	private static EtaError fromJSON(EtaError er, JSONObject error) {
		if (er == null) er = new EtaError();
		if (error == null) return er;
		
		try {
			er.setId(error.getString("id"));
			er.setCode(error.getInt("code"));
			er.setMessage(error.getString("message"));
			er.setDetails(error.getString("details"));
			er.setTime(System.currentTimeMillis());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return er;
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
				.append("]").toString();
	}
}
