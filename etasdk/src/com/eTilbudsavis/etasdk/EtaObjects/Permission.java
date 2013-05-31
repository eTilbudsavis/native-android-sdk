package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Permission implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, ArrayList<String>> perm = new HashMap<String, ArrayList<String>>();
	
	public Permission(JSONObject permission) {
		
		try {
			
			JSONArray groups = permission.names();
			for (int i = 0; i < groups.length() ; i++) {
				
				String group = groups.get(i).toString();
				JSONArray jArray = permission.getJSONArray(group);
				ArrayList<String> permissions = new ArrayList<String>();
				
				for (int j = 0; j < jArray.length() ; j++ ) {
					permissions.add(jArray.get(j).toString());
				}
				
				perm.put(group, permissions);
				
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Prints this object
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[");
		
		Iterator<String> it = perm.keySet().iterator();
		while (it.hasNext()) {
			String group = (String) it.next();
			sb.append(group).append("[");
			for (String permission : perm.get(group)) {
				sb.append(permission).append(", ");
			}
			sb.append("]");
		}
		return sb.append("]").toString();
	}
	
}
