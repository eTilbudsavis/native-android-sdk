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
	
	private HashMap<String, ArrayList<String>> perm = new HashMap<String, ArrayList<String>>();
	
	public Permission() {
	}

	public static Permission fromJSON(String permission) {
		Permission p = new Permission();
		if (permission == null)
			return p;
		try {
			p = fromJSON(p, new JSONObject(permission));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}

	@SuppressWarnings("unchecked")
	public static Permission fromJSON(JSONObject permission) {
		return fromJSON(new Permission(), permission);
	}
	
	public static Permission fromJSON(Permission p, JSONObject permission) {
		if (p == null) p = new Permission();
		if (permission == null) return p;
		
		try {
			
			JSONArray groups = permission.names();
			if (groups == null) {
				EtaLog.d(TAG, "Permission is empty. Reddis error!");
				return p;
			}
			
			for (int i = 0; i < groups.length() ; i++) {
				
				String group = groups.get(i).toString();
				JSONArray jArray = permission.getJSONArray(group);
				ArrayList<String> permissions = new ArrayList<String>();
				
				for (int j = 0; j < jArray.length() ; j++ ) {
					permissions.add(jArray.get(j).toString());
				}
				
				p.getAll().put(group, permissions);
				
			}
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return p;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Permission p) {
		JSONObject o = new JSONObject();
		try {
			Iterator<String> it = p.getAll().keySet().iterator();
			while (it.hasNext()) {
				JSONArray jArray = new JSONArray();
				String name = (String) it.next();
				for (String value : p.getAll().get(name))
					jArray.put(value);
				
				o.put(name, jArray);
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public ArrayList<String> get(String key) {
		return perm.get(key);
	}

	public Permission put(String key, ArrayList<String> permission) {
		perm.put(key, permission);
		return this;
	}
	
	public HashMap<String, ArrayList<String>> getAll() {
		return perm;
	}
	
	public Permission putAll(HashMap<String, ArrayList<String>> permissions) {
		perm.putAll(permissions);
		return this;
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
