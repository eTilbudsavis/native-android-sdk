package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Pieces implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int mFrom;
	private int mTo;
	
	public Pieces() {
		mFrom = 0;
		mTo = 0;
	}
	
	public Pieces(JSONObject pieces) {
		try {
			mFrom = pieces.getInt("from");
			mTo = pieces.getInt("to");
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
		.append("Pieces: { ")
		.append("From: ").append(mFrom)
		.append(", To: ").append(mTo)
		.append("}").toString();
		
	}
	

}
