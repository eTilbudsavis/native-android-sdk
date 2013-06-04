package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Quantity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Quantity";
	
	private Unit mUnit;
	private Size mSize;
	private Pieces mPieces;
	
	public Quantity() {
		mUnit = new Unit();
		mSize = new Size();
		mPieces = new Pieces();
	}
	
	public Quantity(JSONObject quantity) {
		try {
			mUnit = quantity.getString("unit").equals("null") ? null : new Unit(quantity.getJSONObject("unit"));
			mSize = quantity.getString("size").equals("null") ? null : new Size(quantity.getJSONObject("size"));
			mPieces = quantity.getString("pieces").equals("null") ? null : new Pieces(quantity.getJSONObject("pieces"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Unit getUnit() {
		return mUnit;
	}

	public Quantity setUnit(Unit unit) {
		mUnit = unit;
		return this;
	}

	public Size getSize() {
		return mSize;
	}

	public Quantity setSize(Size size) {
		mSize = size;
		return this;
	}
	
	public Pieces getPieces() {
		return mPieces;
	}

	public Quantity setPieces(Pieces pieces) {
		mPieces = pieces;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Quantity))
			return false;

		Quantity q = (Quantity)o;
		return mUnit == null ? q.getUnit() == null : mUnit.equals(q.getUnit()) &&
				mSize == null ? q.getSize() == null : mSize.equals(q.getSize()) &&
				mPieces == null ? q.getPieces() == null : mPieces.equals(q.getPieces());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("unit=").append(mUnit == null ? "null" : mUnit.toString())
		.append("size=").append(mSize == null ? "null" : mSize.toString())
		.append("pieces=").append(mPieces == null ? "null" : mPieces.toString())
		.append("]").toString();
		
	}
}
