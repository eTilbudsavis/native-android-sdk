package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Pages {
	
	private ArrayList<String> mThumb;
	private ArrayList<String> mView;
	private ArrayList<String> mZoom;
	
	public Pages(JSONObject pages) {
		
		try {
			JSONArray jArray = pages.getJSONArray("thumb");
			for (int i = 0 ; i < jArray.length() ; i++ ) {
				mThumb.add((String)jArray.get(i));
			}
			jArray = pages.getJSONArray("view");
			for (int i = 0 ; i < jArray.length() ; i++ ) {
				mView.add((String)jArray.get(i));
			}
			jArray = pages.getJSONArray("zoom");
			for (int i = 0 ; i < jArray.length() ; i++ ) {
				mZoom.add((String)jArray.get(i));
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
	
}
