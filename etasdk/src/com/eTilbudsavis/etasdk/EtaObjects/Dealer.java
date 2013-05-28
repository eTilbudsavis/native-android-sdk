package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Sort;

public class Dealer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Sort a list by name in ascending order. (smallest to largest) */
	public static final String SORT_NAME = Sort.NAME;

	/** Sort a list by name in descending order. (largest to smallest)*/
	public static final String SORT_NAME_DESC = Sort.NAME_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Sort.CREATED_DESC;

	/** Parameter for getting a list of specific dealer id's */
	public static final String PARAM_IDS = "dealer_ids";

	/** Endpoint for dealer list resource */
	public static final String ENDPOINT_LIST = Endpoint.DEALER_LIST;

	/** Endpoint for a single dealer resource */
	public static final String ENDPOINT_ID = Endpoint.DEALER_ID;

	/** Endpoint for searching dealers */
	public static final String ENDPOINT_SEARCH = Endpoint.DEALER_SEARCH;
	
	private String mId;
	private String mErn;
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;

	public Dealer(String name, int color, int pageflipColor) {
		mName = name;
		mColor = color;
		mPageflip = new Pageflip(pageflipColor);
	}
	
	public Dealer(JSONObject dealer) {
		try {
			mId = dealer.getString("id");
			mErn = dealer.getString("ern");
			mName = dealer.getString("name");
			mUrlName = dealer.getString("url_name");
			mWebsite = dealer.getString("website");
			mLogo = dealer.getString("logo");
			mColor = dealer.getInt("color");
			mPageflip = new Pageflip(dealer.getJSONObject("pageflip"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Dealer setId(String id) {
		mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	public Dealer setErn(String ern) {
		mErn = ern;
		return this;
	}

	public String getErn() {
		return mErn;
	}

	public Dealer setName(String name) {
		mName = name;
		return this;
	}

	public String getName() {
		return mName;
	}

	public Dealer setUrlName(String url) {
		mUrlName = url;
		return this;
	}

	public String getUrlName() {
		return mUrlName;
	}

	public Dealer setWebsite(String website) {
		mWebsite = website;
		return this;
	}

	public String getWebsite() {
		return mWebsite;
	}

	public Dealer setLogo(String logo) {
		mLogo = logo;
		return this;
	}

	public String getLogo() {
		return mLogo;
	}

	public Dealer setLogo(int color) {
		mColor = color;
		return this;
	}

	public int getColor() {
		return mColor;
	}

	public Dealer setPageflip(Pageflip pageflip) {
		mPageflip = pageflip;
		return this;
	}

	public Pageflip getPageflip() {
		return mPageflip;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Dealer))
			return false;

		Dealer dealer = (Dealer)o;
		return mId.equals(dealer.getId()) &&
				mErn.equals(dealer.getErn()) &&
				mName.equals(dealer.getName()) &&
				mUrlName.equals(dealer.getUrlName()) &&
				mWebsite.equals(dealer.getWebsite()) &&
				mLogo.equals(dealer.getLogo()) &&
				mColor.equals(dealer.getColor()) &&
				mPageflip == null ? dealer.getPageflip() == null : mPageflip.equals(dealer.getPageflip());
	}

	/**
	 * Returns a human readable string containing id, heading, dealer of the offer. 
	 * E.g. <pre>Dealer { Name: Beer, Id: 1x2y3z, Url: http://www.beer.com}</pre>
	 * @return <li> Dealer digest as a string 
	 */
	@Override
	public String toString() {
		return new StringBuilder()
		.append("Dealer: { ")
		.append("Name: ").append(mName)
		.append(", Id: ").append(mId)
		.append(", Url: ").append(mUrlName)
		.append("}").toString();
		
	}
}
