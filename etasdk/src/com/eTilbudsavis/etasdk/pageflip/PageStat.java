package com.eTilbudsavis.etasdk.pageflip;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request.Method;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.Utils.Api;

public class PageStat {
	
	public static final String TAG = Eta.TAG_PREFIX + PageStat.class.getSimpleName();

	private static final boolean LOG = false;
	
	private final String mCatalogId;
	private final String mViewSession;
	private final boolean mLandscape;
	private final int[] mPages;
	private long mViewStart = 0;
	private long mZoomStart = 0;
	private long mZoomAccumulated = 0;
	
	public PageStat(String catalogId, String viewSessionUuid, int[] pages, boolean landscape) {
		mCatalogId = catalogId;
		mViewSession = viewSessionUuid;
		mPages = pages;
		mLandscape = landscape;
	}
	
	public void collectView() {
		if (mViewStart!=0) {
			log("viewCollect");
			collectZoom();
			long now = System.currentTimeMillis();
			long duration = (now - mViewStart) - mZoomAccumulated;
			collect(true, duration);
		}
		mViewStart = 0;
	}
	
	public void startView() {
		if (mViewStart==0) {
			log("viewStart");
			mViewStart = System.currentTimeMillis();
			mZoomAccumulated = 0;
		}
	}
	
	public void startZoom() {
		if (mZoomStart==0) {
			log("zoomStart");
			mZoomStart = System.currentTimeMillis();
		}
	}
	
	public void collectZoom() {
		if (mZoomStart!=0) {
			log("zoomCollect");
			long duration = System.currentTimeMillis() - mZoomStart;
			mZoomAccumulated += duration;
			collect(false, duration);
		}
		mZoomStart = 0;
	}
	
	private void collect(boolean isView, long duration) {
		
		final JSONObject body = getCollectData(isView, duration);
		String url = Api.Endpoint.catalogCollect(mCatalogId);
		JsonObjectRequest r = new JsonObjectRequest(Method.POST, url, body, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
//				print(body, response, error);
			}
		});
		Eta.getInstance().add(r);
		
	}
	
	private void print(JSONObject body, JSONObject response, EtaError error) {
		String format = null;
		if (response== null) {
			format = "ERROR - %s";
		} else {
			format = "OK - %s";
		}
		EtaLog.d(TAG, String.format(format, body.toString()));
	}
	
	private JSONObject getCollectData(boolean isView, long ms) {
		JSONObject o = new JSONObject();
		try {
			o.put("type", isView ? "view" : "zoom");
			o.put("ms", ms);
			o.put("orientation", mLandscape ? "landscape" : "portrait");
			o.put("pages", PageflipUtils.join(",", mPages));
			o.put("view_session", mViewSession);
		} catch (JSONException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		}
		return o;
	}
	
	private void log(String s) {
		if (LOG) {
			EtaLog.d(TAG, "[" + PageflipUtils.join("-", mPages) + "] " + s);
		}
	}
	
}
