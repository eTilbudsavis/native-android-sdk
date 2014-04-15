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
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Permission extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Permission";
	
	private HashMap<String, ArrayList<String>> mPermissions = new HashMap<String, ArrayList<String>>();
	
	public Permission() {
		
	}
	
	public static Permission fromJSON(JSONObject permission) {
		return fromJSON(new Permission(), permission);
	}
	
	public static Permission fromJSON(Permission p, JSONObject permission) {
		if (p == null) p = new Permission();
		if (permission == null) return p;
		
		try {
			
			JSONArray groups = permission.names();
			if (groups == null) {
				return p;
			}
			
			for (int i = 0; i < groups.length() ; i++) {
				
				String group = groups.get(i).toString();
				JSONArray jArray = permission.getJSONArray(group);
				ArrayList<String> permissions = new ArrayList<String>();
				
				for (int j = 0; j < jArray.length() ; j++ ) {
					permissions.add(jArray.get(j).toString());
				}
				
				p.getPermissions().put(group, permissions);
				
			}
			
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		
		return p;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			Iterator<String> it = getPermissions().keySet().iterator();
			while (it.hasNext()) {
				JSONArray jArray = new JSONArray();
				String name = (String) it.next();
				for (String value : getPermissions().get(name)) {
					jArray.put(value);
				}				
				o.put(name, jArray);
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		return o;
	}
	
	public ArrayList<String> getGroupPermissions(String group) {
		return mPermissions.get(group);
	}
	
	public Permission put(String group, ArrayList<String> permissions) {
		if (mPermissions.containsKey(group)) {
			mPermissions.get(group).addAll(permissions);
		} else {
			mPermissions.put(group, permissions);
		}
		return this;
	}
	
	public HashMap<String, ArrayList<String>> getPermissions() {
		return mPermissions;
	}
	
	public Permission putAll(HashMap<String, ArrayList<String>> permissions) {
		mPermissions.putAll(permissions);
		return this;
	}
	
}
