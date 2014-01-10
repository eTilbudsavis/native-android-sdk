package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Pieces extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pieces";
	
	private int mFrom = 1;
	private int mTo = 1;
	
	public Pieces() {
	}
	
	public static Pieces fromJSON(String pieces) {
		Pieces p = new Pieces();
		try {
			p = fromJSON(p, new JSONObject(pieces));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}
	
	public static Pieces fromJSON(JSONObject pieces) {
		return fromJSON(new Pieces(), pieces);
	}
	
	public static Pieces fromJSON(Pieces p, JSONObject pieces) {
		if (p == null) p = new Pieces();
		if (pieces == null) return p;
		
		try {
			p.setFrom(pieces.getInt(ServerKey.FROM));
			p.setTo(pieces.getInt(ServerKey.TO));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Pieces p) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.FROM, p.getFrom());
			o.put(ServerKey.TO, p.getTo());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public int getFrom() {
		return mFrom;
	}


	public Pieces setFrom(int from) {
		mFrom = from;
		return this;
	}


	public int getTo() {
		return mTo;
	}


	public Pieces setTo(int to) {
		mTo = to;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mFrom;
		result = prime * result + mTo;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pieces other = (Pieces) obj;
		if (mFrom != other.mFrom)
			return false;
		if (mTo != other.mTo)
			return false;
		return true;
	}
	
	
}
