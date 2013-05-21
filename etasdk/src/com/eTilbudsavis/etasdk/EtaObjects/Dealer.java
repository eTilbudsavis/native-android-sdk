package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Dealer implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Sort a list by name in ascending order. (smallest to largest) */
	public static final String SORT_NAME = "name";

	/** Sort a list by name in descending order. (largest to smallest)*/
	public static final String SORT_NAME_DESC = "-" + SORT_NAME;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = "created";

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = "-" + SORT_CREATED;

	/** Parameter for getting a list of specific dealer id's */
	public static final String DEALER_IDS = "dealer_ids";

	private String mId;
	private String mName;
	private String mUrl;
	private String mWebsite;
	private Branding mBranding;
	private String mLogo;

	public Dealer(JSONObject dealer) {
		try {
			mId = dealer.getString("id");
			mName = dealer.getString("name");
			mUrl = dealer.getString("url");
			mWebsite = dealer.getString("website");
			mBranding = new Branding(dealer.getJSONObject("branding"));
			mLogo = dealer.getString("logo");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Dealer setId(String id) {
		this.mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	public Dealer setName(String name) {
		this.mName = name;
		return this;
	}

	public String getName() {
		return mName;
	}

	public Dealer setUrl(String url) {
		this.mUrl = url;
		return this;
	}

	public String getUrl() {
		return mUrl;
	}

	public Dealer setWebsite(String website) {
		this.mWebsite = website;
		return this;
	}

	public String getWebsite() {
		return mWebsite;
	}

	public Dealer setBranding(Branding branding) {
		this.mBranding = branding;
		return this;
	}

	public Branding getBranding() {
		return mBranding;
	}

	public Dealer setLogo(String logo) {
		this.mLogo = logo;
		return this;
	}

	public String getLogo() {
		return mLogo;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Dealer))
			return false;

		Dealer dealer = (Dealer)o;
		return mId.equals(dealer.getId()) &&
				mName.equals(dealer.getName()) &&
				mUrl.equals(dealer.getUrl()) &&
				mWebsite.equals(dealer.getWebsite()) &&
				mBranding.equals(dealer.getBranding()) &&
				mLogo.equals(dealer.getLogo());
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
		.append(", Url: ").append(mUrl)
		.append("}").toString();
		
	}
}
