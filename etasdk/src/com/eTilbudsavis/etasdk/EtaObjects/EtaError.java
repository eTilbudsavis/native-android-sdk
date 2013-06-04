package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class EtaError implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaError";
	
	private String mId;
	private int mCode;
	private String mMessage;
	private String mDetails;
	private long mTime;
	
	public EtaError(JSONObject error) {
		try {
			mId = error.getString("id");
			mCode = error.getInt("code");
			mMessage = error.getString("message");
			mDetails = error.getString("details");
			mTime = System.currentTimeMillis();
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
				.append(", time=").append(mTime)
				.append("]").toString();
	}
}
