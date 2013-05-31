package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Size implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private double mFrom;
	private double mTo;
	
	public Size() {
		mFrom = 0.0;
		mTo = 0.0;
	}
	
	public Size(JSONObject size) {
		try {
			mFrom = size.getDouble("from");
			mTo = size.getDouble("to");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public double getFrom() {
		return mFrom;
	}


	public Size setFrom(double from) {
		mFrom = from;
		return this;
	}


	public double getTo() {
		return mTo;
	}


	public Size setTo(double to) {
		mTo = to;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Size))
			return false;

		Size s = (Size)o;
		return mFrom == s.getFrom() &&
				mTo == s.getTo();
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
