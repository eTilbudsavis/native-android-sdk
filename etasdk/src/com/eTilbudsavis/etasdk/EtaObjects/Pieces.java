package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Pieces implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String S_FROM = "from";
	private static final String S_TO = "to";
	
	public static final String TAG = "Pieces";
	
	private int mFrom = 0;
	private int mTo = 0;
	
	public Pieces() {
	}
	
	public static Pieces fromJSON(String pieces) {
		Pieces p = new Pieces();
		try {
			p = fromJSON(p, new JSONObject(pieces));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
			p.setFrom(pieces.getInt(S_FROM));
			p.setTo(pieces.getInt(S_TO));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
			if (Eta.DEBUG)
				e.printStackTrace();
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
