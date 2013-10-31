package com.eTilbudsavis.etasdk.NetworkHelpers;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class EtaError extends Exception {

	public static final String TAG = "EtaError";

    private static final String ID = "id";
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";
    
	public final JSONObject serverMessage;

	public EtaError() {
		serverMessage = new JSONObject();
	}

	public EtaError(NetworkResponse r) {
		serverMessage = parseJSON(r);
	}

	public EtaError(String detailMessage) {
		super(detailMessage);
		serverMessage = new JSONObject();
	}

	public EtaError(Throwable t) {
		super(t);
		serverMessage = new JSONObject();
	}

	public EtaError(String detailMessage, Throwable t) {
		super(detailMessage, t);
		serverMessage = new JSONObject();
	}

	private JSONObject parseJSON(NetworkResponse r) {
		try {
			return new JSONObject(new String(r.data));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			return new JSONObject();
		}
	}

	public int getCode() {
		try {
			return serverMessage.getInt(CODE);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			return 0;
		}
	}

	public String getMessage() {
		try {
			return serverMessage.getString(MESSAGE);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			return "";
		}
	}

	public String getDetails() {
		try {
			return serverMessage.getString(DETAILS);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			return "";
		}
	}

	public String getId() {
		try {
			return serverMessage.getString(ID);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
			return "";
		}
	}

}
