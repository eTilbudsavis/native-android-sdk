package com.eTilbudsavis.etasdk.pageflip;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;
import com.eTilbudsavis.etasdk.Network.Request.Method;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.Utils.Api;

public class StatsCollectImpl implements StatsCollect {
	
	public static final String TAG = StatsCollectImpl.class.getSimpleName();
	
	Catalog mCatalog;
	private long mCollectViewSession = System.currentTimeMillis();
	private long mCollectViewStart = 0;
	private long mCollectZoomStart = 0;
	private long mCollectZoomAccumulated = 0;
	
	public StatsCollectImpl(Catalog c) {
		mCatalog = c;
		mCollectViewStart = System.currentTimeMillis();
		mCollectZoomStart = System.currentTimeMillis();
	}
	
	public void collectView(boolean landscape, int[] pages) {
		long now = System.currentTimeMillis();
		long duration = (now - mCollectViewStart) - mCollectZoomAccumulated;
		mCollectViewStart = now;
		mCollectZoomAccumulated = 0;
		collect(true, duration, landscape, pages);
	}
	
	public void collectZoom(boolean start, boolean landscape, int[] pages) {
		if (start) {
			mCollectZoomStart = System.currentTimeMillis();
		} else {
			long duration = System.currentTimeMillis() - mCollectZoomStart;
			mCollectZoomAccumulated += duration;
			collect(false, duration, landscape, pages);
		}
	}
	
	private void collect(boolean isView, long duration, boolean landscape, int[] pages) {
		
		JSONObject body = getCollectData(isView, duration, landscape, pages);
		String url = Api.Endpoint.catalogCollect(mCatalog.getId());
		
		JsonObjectRequest r = new JsonObjectRequest(Method.POST, url, body, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				if (response!= null) {
//					EtaLog.d(TAG, response.toString());
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
			}
		});
		EtaLog.d(TAG, r.getUrl());
		Eta.getInstance().add(r);
		
	}
	
	private JSONObject getCollectData(boolean isView, long ms, boolean isLandscape, int[] pages) {
		JSONObject o = new JSONObject();
		try {
			o.put("type", isView ? "view" : "zoom");
			o.put("ms", ms);
			o.put("orientation", isLandscape ? "landscape" : "portrait");
			o.put("pages", PageflipUtils.join(",", pages));
			o.put("view_session", mCollectViewSession);
		} catch (JSONException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		}
		return o;
	}
	
}
