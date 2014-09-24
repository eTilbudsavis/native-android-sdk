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
package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Page implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = -1467489830915012500L;

	public static final String TAG = Page.class.getSimpleName();
	
	private String mThumb;
	private String mView;
	private String mZoom;
	
	public static List<Page> fromJSON(JSONArray pages) {
		List<Page> list = new ArrayList<Page>();
		for (int i = 0 ; i < pages.length() ; i++) {
			try {
				list.add(fromJSON(pages.getJSONObject(i)));
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		return list;
	}
	
	public static Page fromJSON(JSONObject page) {
		Page p = new Page();
		if (page == null) {
			return p;
		}
		
		p.setThumb(Json.valueOf(page, JsonKey.THUMB));
		p.setView(Json.valueOf(page, JsonKey.VIEW));
		p.setZoom(Json.valueOf(page, JsonKey.ZOOM));
		
		return p;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.THUMB, getThumb());
			o.put(JsonKey.VIEW, getView());
			o.put(JsonKey.ZOOM, getZoom());
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public String getThumb() {
		return mThumb;
	}
	
	public Page setThumb(String thumb) {
		mThumb = thumb;
		return this;
	}
	
	public String getView() {
		return mView;
	}
	
	public Page setView(String view) {
		mView = view;
		return this;
	}
	
	public String getZoom() {
		return mZoom;
	}
	
	public Page setZoom(String zoom) {
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
		Page other = (Page) obj;
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
