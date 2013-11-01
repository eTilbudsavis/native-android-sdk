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
	
	@SuppressWarnings("unchecked")
	public static Pieces fromJSON(JSONObject pieces) {
		return fromJSON(new Pieces(), pieces);
	}
	
	public static Pieces fromJSON(Pieces p, JSONObject pieces) {
		if (p == null) p = new Pieces();
		if (pieces == null) return p;
		
		try {
			p.setFrom(pieces.getInt(S_FROM));
			p.setTo(pieces.getInt(S_TO));
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
			o.put(S_FROM, p.getFrom());
			o.put(S_TO, p.getTo());
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pieces))
			return false;

		Pieces p = (Pieces)o;
		return mFrom == p.getFrom() &&
				mTo == p.getTo();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("from=").append(mFrom)
		.append(", to=").append(mTo)
		.append("]").toString();
		
	}
	

}
