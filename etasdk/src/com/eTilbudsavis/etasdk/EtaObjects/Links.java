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

public class Links extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Links";

	private String mWebshop;
	
	public Links() {
		
	}
	
	public static Links fromJSON(JSONObject links) {
		return fromJSON(new Links(), links);
	}
	
	public static Links fromJSON(Links l, JSONObject links) {
		if (l == null) l = new Links();
		if (links == null) return l;
		
		l.setWebshop(Json.valueOf(links, ServerKey.WEBSHOP));
		
		return l;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.WEBSHOP, Json.nullCheck(getWebshop()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
