package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

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
			EtaLog.d(TAG, e);
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
			q.setUnit(quantity.isNull(Key.UNIT) ? null : Unit.fromJSON(quantity.getJSONObject(Key.UNIT)));
			q.setSize(quantity.isNull(Key.SIZE) ? null : Size.fromJSON(quantity.getJSONObject(Key.SIZE)));
			q.setPieces(quantity.isNull(Key.PIECES) ? null : Pieces.fromJSON(quantity.getJSONObject(Key.PIECES)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return q;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Quantity q) {
		JSONObject o = new JSONObject();
		try {
			o.put(Key.UNIT, q.getUnit() == null ? null : q.getUnit().toJSON());
			o.put(Key.SIZE, q.getSize() == null ? null : q.getSize().toJSON());
			o.put(Key.PIECES, q.getPieces() == null ? null : q.getPieces().toJSON());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
