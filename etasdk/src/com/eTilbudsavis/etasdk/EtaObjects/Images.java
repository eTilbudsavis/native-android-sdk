package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Images implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String mView;
	private String mZoom;
	private String mThumb;

	public Images() {
		mView = "";
		mZoom = "";
		mThumb = "";
	}
	
	public Images(JSONObject image) {
    	
    	try {
			mView = image.getString("view");
			mZoom = image.getString("zoom");
			mThumb = image.getString("thumb");
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
		
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
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("view=").append(mView)
		.append("zoom=").append(mZoom)
		.append("thumb=").append(mThumb)
		.append("]").toString();
	}
	
}
