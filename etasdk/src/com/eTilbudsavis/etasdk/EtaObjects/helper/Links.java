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

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.ServerKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Links implements EtaObject<JSONObject>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = Links.class.getSimpleName();

	private String mWebshop;
	
	public static Links fromJSON(JSONObject links) {
		Links l = new Links();
		if (links == null) {
			return l;
		}
		
		l.setWebshop(Json.valueOf(links, ServerKey.WEBSHOP));
		
		return l;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.WEBSHOP, Json.nullCheck(getWebshop()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	
	public void setWebshop(String url) {
		mWebshop = url;
	}
	
	public String getWebshop() {
		return mWebshop;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mWebshop == null) ? 0 : mWebshop.hashCode());
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
		Links other = (Links) obj;
		if (mWebshop == null) {
			if (other.mWebshop != null)
				return false;
		} else if (!mWebshop.equals(other.mWebshop))
			return false;
		return true;
	}

	
}
