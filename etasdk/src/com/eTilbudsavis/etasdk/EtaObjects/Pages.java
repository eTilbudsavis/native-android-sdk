/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
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
		JSONObject o = new JSONObject();
		try {
			JSONArray aThumb = new JSONArray();
			JSONArray aView = new JSONArray();
			JSONArray aZoom = new JSONArray();
			for (String s : getThumb())
				aThumb.put(s);
			
			for (String s : getView())
				aView.put(s);
			
			for (String s : getZoom())
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
