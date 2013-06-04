package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Pages implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pages";
	
	private ArrayList<String> mThumb = new ArrayList<String>();
	private ArrayList<String> mView = new ArrayList<String>();
	private ArrayList<String> mZoom = new ArrayList<String>();
	
	public Pages(JSONObject pages) {
		
		try {
			JSONArray jArray = pages.getJSONArray("thumb");
			int i;
			for (i = 0 ; i < jArray.length() ; i++ ) {
				mThumb.add(jArray.getString(i));
			}
			jArray = pages.getJSONArray("view");
			for (i = 0 ; i < jArray.length() ; i++ ) {
				mView.add(jArray.getString(i));
			}
			jArray = pages.getJSONArray("zoom");
			for (i = 0 ; i < jArray.length() ; i++ ) {
				mZoom.add(jArray.getString(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
