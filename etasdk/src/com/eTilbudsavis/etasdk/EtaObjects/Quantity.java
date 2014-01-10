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
	
	public static Quantity fromJSON(JSONObject quantity) {
		return fromJSON(new Quantity(), quantity);
	}
	
	public static Quantity fromJSON(Quantity q, JSONObject quantity) {
		if (q == null) q = new Quantity();
		if (quantity == null) return q;
		
		try {
			q.setUnit(quantity.isNull(ServerKey.UNIT) ? null : Unit.fromJSON(quantity.getJSONObject(ServerKey.UNIT)));
			q.setSize(quantity.isNull(ServerKey.SIZE) ? null : Size.fromJSON(quantity.getJSONObject(ServerKey.SIZE)));
			q.setPieces(quantity.isNull(ServerKey.PIECES) ? null : Pieces.fromJSON(quantity.getJSONObject(ServerKey.PIECES)));
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
			o.put(ServerKey.UNIT, q.getUnit() == null ? null : q.getUnit().toJSON());
			o.put(ServerKey.SIZE, q.getSize() == null ? null : q.getSize().toJSON());
			o.put(ServerKey.PIECES, q.getPieces() == null ? null : q.getPieces().toJSON());
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mPieces == null) ? 0 : mPieces.hashCode());
		result = prime * result + ((mSize == null) ? 0 : mSize.hashCode());
		result = prime * result + ((mUnit == null) ? 0 : mUnit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Quantity other = (Quantity) obj;
		if (mPieces == null) {
			if (other.mPieces != null)
				return false;
		} else if (!mPieces.equals(other.mPieces))
			return false;
		if (mSize == null) {
			if (other.mSize != null)
				return false;
		} else if (!mSize.equals(other.mSize))
			return false;
		if (mUnit == null) {
			if (other.mUnit != null)
				return false;
		} else if (!mUnit.equals(other.mUnit))
			return false;
		return true;
	}
	
	
}
