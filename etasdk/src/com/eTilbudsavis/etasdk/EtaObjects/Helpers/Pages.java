package com.eTilbudsavis.etasdk.EtaObjects.Helpers;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Pages implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pages";

	public static final String S_THUMB = "thumb";
	public static final String S_VIEW = "view";
	public static final String S_ZOOM = "zoom";
	
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
			if (Eta.DEBUG)
				e.printStackTrace();
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
			JSONArray jArray = pages.getJSONArray(S_THUMB);
			int i;
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getThumb().add(jArray.getString(i));
			}
			jArray = pages.getJSONArray(S_VIEW);
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getView().add(jArray.getString(i));
			}
			jArray = pages.getJSONArray(S_ZOOM);
			for (i = 0 ; i < jArray.length() ; i++ ) {
				p.getZoom().add(jArray.getString(i));
			}
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return p;
	}
	
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

			o.put(S_THUMB, aThumb);
			o.put(S_VIEW, aView);
			o.put(S_ZOOM, aZoom);
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pages))
			return false;

		Pages p = (Pages)o;
		return mThumb == null ? p.getThumb() == null : mThumb.equals(p.getThumb()) &&
				mView == null ? p.getView() == null : mView.equals(p.getView()) &&
				mZoom == null ? p.getZoom() == null : mZoom.equals(p.getZoom());
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[");
		if (everything) {
			sb.append("view=").append(mView.toString())
			.append(", zoom=").append(mZoom.toString())
			.append(", thumb=").append(mThumb.toString());
		} else {
			sb.append("viewCount=").append(mView.size())
			.append(", zoomCount=").append(mZoom.size())
			.append(", thumbCount=").append(mThumb.size());
		}
		return sb.append("]").toString();
	}
	
}
