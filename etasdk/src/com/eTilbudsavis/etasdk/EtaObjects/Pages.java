package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Pages extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pages";

	private ArrayList<String> mThumb = new ArrayList<String>();
	private ArrayList<String> mView = new ArrayList<String>();
	private ArrayList<String> mZoom = new ArrayList<String>();
	
	public Pages() {
	}
	
	public static Pages fromJSON(String pages) {
		Pages p = new Pages();
		try {
			p = fromJSON(p, new JSONObject(pages));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}
	
	public static Pages fromJSON(JSONObject pages) {
		return fromJSON(new Pages(), pages);
	}
	
	public static Pages fromJSON(Pages p, JSONObject pages) {
		if (p == null) p = new Pages();
		if (pages == null) return p;
		
		try {
			JSONArray jArray = pages.getJSONArray(ServerKey.THUMB);
			int i;
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getThumb().add(jArray.getString(i));
			}
			jArray = pages.getJSONArray(ServerKey.VIEW);
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getView().add(jArray.getString(i));
			}
			jArray = pages.getJSONArray(ServerKey.ZOOM);
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getZoom().add(jArray.getString(i));
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}

	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Pages p) {
		JSONObject o = new JSONObject();
		try {
			JSONArray aThumb = new JSONArray();
			JSONArray aView = new JSONArray();
			JSONArray aZoom = new JSONArray();
			for (String s : p.getThumb())
				aThumb.put(s);
			
			for (String s : p.getView())
				aView.put(s);
			
			for (String s : p.getZoom())
				aZoom.put(s);

			o.put(ServerKey.THUMB, aThumb);
			o.put(ServerKey.VIEW, aView);
			o.put(ServerKey.ZOOM, aZoom);
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public ArrayList<String> getThumb() {
		return mThumb;
	}

	public Pages setThumb(ArrayList<String> thumb) {
		mThumb = thumb;
		return this;
	}

	public ArrayList<String> getView() {
		return mView;
	}

	public Pages setView(ArrayList<String> view) {
		mView = view;
		return this;
	}

	public ArrayList<String> getZoom() {
		return mZoom;
	}

	public Pages setZoom(ArrayList<String> zoom) {
		mZoom = zoom;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mThumb == null) ? 0 : mThumb.hashCode());
		result = prime * result + ((mView == null) ? 0 : mView.hashCode());
		result = prime * result + ((mZoom == null) ? 0 : mZoom.hashCode());
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
		Pages other = (Pages) obj;
		if (mThumb == null) {
			if (other.mThumb != null)
				return false;
		} else if (!mThumb.equals(other.mThumb))
			return false;
		if (mView == null) {
			if (other.mView != null)
				return false;
		} else if (!mView.equals(other.mView))
			return false;
		if (mZoom == null) {
			if (other.mZoom != null)
				return false;
		} else if (!mZoom.equals(other.mZoom))
			return false;
		return true;
	}
	
	
	
}
