package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	
}
