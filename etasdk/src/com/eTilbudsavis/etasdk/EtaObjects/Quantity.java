package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Quantity implements Serializable {

	private static final long serialVersionUID = 1L;
	
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
			mUnit = new Unit(quantity.getJSONObject("unit"));
			mSize = new Size(quantity.getJSONObject("size"));
			mPieces = new Pieces(quantity.getJSONObject("pieces"));
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
		.append("Quantity: { ")
		.append(mUnit.toString()).append(", ")
		.append(mSize.toString()).append(", ")
		.append(mPieces.toString())
		.append("}").toString();
		
	}
}
