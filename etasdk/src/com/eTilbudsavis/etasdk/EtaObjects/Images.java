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

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Images extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Images";
	
	private String mView;
	private String mZoom;
	private String mThumb;

	public Images() {
		
	}
	
	public static Images fromJSON(JSONObject images) {
		return fromJSON(new Images(), images);
	}
	
	public static Images fromJSON(Images i, JSONObject image) {
		if (i == null) i = new Images();
		if (image == null) return i;
		
		i.setView(Json.valueOf(image, ServerKey.VIEW));
		i.setZoom(Json.valueOf(image, ServerKey.ZOOM));
		i.setThumb(Json.valueOf(image, ServerKey.THUMB));
		
    	return i;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.VIEW, Json.nullCheck(getView()));
			o.put(ServerKey.ZOOM, Json.nullCheck(getZoom()));
			o.put(ServerKey.THUMB, Json.nullCheck(getThumb()));
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return o;
	}
	
	public String getView() {
		return mView;
	}

	public void setView(String viewUrl) {
		this.mView = viewUrl;
	}

	public String getZoom() {
		return mZoom;
	}

	public void setZoom(String zoomUrl) {
		this.mZoom = zoomUrl;
	}

	public String getThumb() {
		return mThumb;
	}

	public void setThumb(String thumbUrl) {
		this.mThumb = thumbUrl;
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
		Images other = (Images) obj;
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
