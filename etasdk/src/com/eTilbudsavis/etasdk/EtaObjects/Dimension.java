package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Dimension implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private double mWidth;
	private double mHeight;
	
	public Dimension() {
		mWidth = 0.0;
		mHeight = 0.0;
	}
	
	public Dimension(JSONObject dimension) {
    	
    	try {
			mWidth = dimension.getDouble("width");
			mHeight = dimension.getDouble("height");
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
		
	}

	public double getWidth() {
		return mWidth;
	}

	public Dimension setWidth(double width) {
		mWidth = width;
		return this;
	}

	public double getHeight() {
		return mHeight;
	}

	public Dimension setHeight(double height) {
		mHeight = height;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Dimension))
			return false;

		Dimension d = (Dimension)o;
		return 
				mWidth == d.getWidth() &&
				mHeight == d.getHeight();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append("Dimension { ")
		.append("Width: ").append(mWidth)
		.append(", Height: ").append(mHeight)
		.append(" }").toString();
	}

}
