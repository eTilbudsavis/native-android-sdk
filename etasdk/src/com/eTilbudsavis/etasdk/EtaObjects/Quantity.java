package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Quantity extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Quantity";
	
	private Unit mUnit;
	private Size mSize;
	private Pieces mPieces;
	
	public Quantity() {
	}
	
	public static Quantity fromJSON(String quantity) {
		Quantity q = new Quantity();
		try {
			q = fromJSON(q, new JSONObject(quantity));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return q;
	}
	
	@SuppressWarnings("unchecked")
	public static Quantity fromJSON(JSONObject quantity) {
		return fromJSON(new Quantity(), quantity);
	}
	
	public static Quantity fromJSON(Quantity q, JSONObject quantity) {
		if (q == null) q = new Quantity();
		if (quantity == null) return q;
		
		try {
			q.setUnit(quantity.isNull(S_UNIT) ? null : Unit.fromJSON(quantity.getJSONObject(S_UNIT)));
			q.setSize(quantity.isNull(S_SIZE) ? null : Size.fromJSON(quantity.getJSONObject(S_SIZE)));
			q.setPieces(quantity.isNull(S_PIECES) ? null : Pieces.fromJSON(quantity.getJSONObject(S_PIECES)));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return q;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Quantity q) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_UNIT, q.getUnit() == null ? null : q.getUnit().toJSON());
			o.put(S_SIZE, q.getSize() == null ? null : q.getSize().toJSON());
			o.put(S_PIECES, q.getPieces() == null ? null : q.getPieces().toJSON());
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return o;
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
