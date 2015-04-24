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
package com.eTilbudsavis.etasdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;

public class Images implements IJson<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Constants.getTag(Images.class);
	
	private String mView;
	private String mZoom;
	private String mThumb;

	public static Parcelable.Creator<Images> CREATOR = new Parcelable.Creator<Images>(){
		public Images createFromParcel(Parcel source) {
			return new Images(source);
		}
		public Images[] newArray(int size) {
			return new Images[size];
		}
	};
	
	public Images() {
		
	}
	
	public static List<Images> fromJSON(JSONArray images) {
		List<Images> list = new ArrayList<Images>();
		for (int i = 0 ; i < images.length() ; i++) {
			try {
				list.add(fromJSON(images.getJSONObject(i)));
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		return list;
	}
	
	public static Images fromJSON(JSONObject image) {
		Images i = new Images();
		if (image == null) {
			return i;
		}
		
		i.setView(Json.valueOf(image, JsonKey.VIEW));
		i.setZoom(Json.valueOf(image, JsonKey.ZOOM));
		i.setThumb(Json.valueOf(image, JsonKey.THUMB));
		
    	return i;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.VIEW, Json.nullCheck(getView()));
			o.put(JsonKey.ZOOM, Json.nullCheck(getZoom()));
			o.put(JsonKey.THUMB, Json.nullCheck(getThumb()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
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
		int result = 1;
		result = prime * result + ((mThumb == null) ? 0 : mThumb.hashCode());
		result = prime * result + ((mView == null) ? 0 : mView.hashCode());
		result = prime * result + ((mZoom == null) ? 0 : mZoom.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
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

	private Images(Parcel in) {
		this.mView = in.readString();
		this.mZoom = in.readString();
		this.mThumb = in.readString();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mView);
		dest.writeString(this.mZoom);
		dest.writeString(this.mThumb);
	}

}
