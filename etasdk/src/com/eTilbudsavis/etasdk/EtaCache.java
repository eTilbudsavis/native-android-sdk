package com.eTilbudsavis.etasdk;

import java.io.Serializable;

import Utils.Utilities;

public class EtaCache implements Serializable {

	private static final long serialVersionUID = 1L;

	// HTML at `PROVIDER_URL` endpoint.
	private String mHtmlCached = "";
	private int mHtmlAcquired = 0;
	private int mHtmlExpire = 15 * 60;

	public EtaCache() {
		
	}

	/**
	 * Returns HTML cache.
	 *
	 * @return Cached HTML as String
	 */
	public String getHtmlCached() {
		return mHtmlCached;
	}

	/**
	 * Sets the cached HTML content.
	 *
	 * @param HTML
	 */
	public void setHtmlCached(String html) {
		// Validate input.
		if (html.matches(".*\\<[^>]+>.*")) {
			mHtmlCached = html;
			mHtmlAcquired = Utilities.getTime();
		}
	}

	/**
	 * Returns the time at which the HTML was cached.
	 * @return HTML caching timestamp
	 */
	public int getHtmlAcquired() {
		return mHtmlAcquired;
	}

	/**public static final int API_PAGE_LIMIT = 25;
	 * Returns the TTL for the HTML cache.
	 * @return TTL for the HTML cache
	 */
	public int getHtmlExpire() {
		return mHtmlExpire;
	}

	/**
	 * Sets the TTL for the HTML cache.
	 * @param TTL in seconds
	 */
	public void setHtmlExpire(int seconds) {
		mHtmlExpire = seconds;
	}


}
