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
	
	public static final String TAG = PageStat.class.getSimpleName();

	private static final boolean LOG = false;
	
	private final String mCatalogId;
	private final String mViewSession;
	private final boolean mLandscape;
	private final int[] mPages;
	private int mViewStartCounter = 0;
	private long mViewStart = 0;
	private long mZoomStart = 0;
	private long mZoomAccumulated = 0;
	private boolean mViewCollectStarted = false;
	private boolean mZoomCollectStarted = false;
	
	public PageStat(String catalogId, String viewSessionUuid, int[] pages, boolean landscape) {
		mCatalogId = catalogId;
		mViewSession = viewSessionUuid;
		mPages = pages;
		mLandscape = landscape;
	}
	
	private String page() {
		return PageflipUtils.join("-", mPages);
	}
	
	public void viewCollect() {
		if (mViewCollectStarted) {
			log("viewCollect");
			zoomCollect();
			long now = System.currentTimeMillis();
			long duration = (now - mViewStart) - mZoomAccumulated;
			String s = "now: %s, start: %s, zoom: %s";
			EtaLog.d(TAG, String.format(s, now, mViewStart, mZoomAccumulated));
			collect(true, duration);
		}
		mViewCollectStarted = false;
	}
	
	public void reset(int eventsNeeded) {
		log("reset");
		mViewStartCounter = 0;
	}
	
	public void viewStart() {
		// We need both a onVisible event, and image loaded event
		if (mViewStartCounter==1) {
			log("viewStart");
			mViewStart = System.currentTimeMillis();
			mZoomAccumulated = 0;
			mViewCollectStarted = true;
		}
		mViewStartCounter++;
	}
	
	public void zoomStart() {
		log("zoomStart");
		mZoomCollectStarted = true;
		mZoomStart = System.currentTimeMillis();
	}
	
	public void zoomCollect() {
		if (mZoomCollectStarted) {
			log("zoomCollect");
			long now = System.currentTimeMillis();
			long duration = now - mZoomStart;
			mZoomAccumulated += duration;
			collect(false, duration);
		}
		mZoomCollectStarted = false;
	}
	
	private void collect(boolean isView, long duration) {
		
		final JSONObject body = getCollectData(isView, duration);
		String url = Api.Endpoint.catalogCollect(mCatalogId);
		JsonObjectRequest r = new JsonObjectRequest(Method.POST, url, body, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				String format = null;
				if (response== null) {
					format = "ERROR - %s";
				} else {
					format = "OK - %s";
				}
				EtaLog.d(TAG, String.format(format, body.toString()));
			}
		});
		Eta.getInstance().add(r);
		
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
			EtaLog.d(TAG, "[" + page() + "] " + s);
		}
	}
	
}
