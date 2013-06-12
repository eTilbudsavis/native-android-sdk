package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Params;
import Utils.Sort;
import android.graphics.Color;

public class Dealer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Dealer";

	/** Sort a list by name in ascending order. (smallest to largest) */
	public static final String SORT_NAME = Sort.NAME;

	/** Sort a list by name in descending order. (largest to smallest)*/
	public static final String SORT_NAME_DESC = Sort.NAME_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Sort.CREATED_DESC;

	/** Parameter for getting a list of specific dealer id's */
	public static final String FILTER_DEALER_IDS = Params.FILTER_DEALER_IDS;

	/** String identifying the query parameter */
	public static final String PARAM_QUERY = Params.QUERY;
	
	/** Endpoint for dealer list resource */
	public static final String ENDPOINT_LIST = Endpoint.DEALER_LIST;

	/** Endpoint for a single dealer resource */
	public static final String ENDPOINT_ID = Endpoint.DEALER_ID;

	/** Endpoint for searching dealers */
	public static final String ENDPOINT_SEARCH = Endpoint.DEALER_SEARCH;
	
	private static final String S_ID = "id";
	private static final String S_ERN = "ern";
	private static final String S_NAME = "name";
	private static final String S_URL_NAME = "url_name";
	private static final String S_WEBSITE = "website";
	private static final String S_LOGO = "logo";
	private static final String S_COLOR = "color";
	private static final String S_PAGEFLIP = "pageflip";
	
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
	
	public Dealer() {
	}

	public static ArrayList<Dealer> fromJSONArray(String dealers) {
		try {
			return fromJSONArray(new JSONArray(dealers));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<Dealer> fromJSONArray(JSONArray dealers) {
		ArrayList<Dealer> list = new ArrayList<Dealer>();
		try {
			for (int i = 0 ; i < dealers.length() ; i++ )
				list.add(Dealer.fromJSON((JSONObject)dealers.get(i)));
			
		} catch (JSONException e) {
			list = null;
			e.printStackTrace();
		}
		return list;
	}
	
	public static Dealer fromJSON(String dealer) {
		try {
			return fromJSON(new Dealer(), new JSONObject(dealer));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Dealer fromJSON(JSONObject dealer) {
		return fromJSON(new Dealer(), dealer);
	}
	
	private static Dealer fromJSON(Dealer d, JSONObject dealer) {
		if (d == null) d = new Dealer();
		if (dealer == null) return d;
		
		try {
			d.setId(dealer.getString(S_ID));
			d.setErn(dealer.getString(S_ERN));
			d.setName(dealer.getString(S_NAME));
			d.setUrlName(dealer.getString(S_URL_NAME));
			d.setWebsite(dealer.getString(S_WEBSITE));
			d.setLogo(dealer.getString(S_LOGO));
			d.setColor(Color.parseColor("#"+dealer.getString(S_COLOR)));
			d.setPageflip(Pageflip.fromJSON(dealer.getJSONObject(S_PAGEFLIP)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public JSONObject toJSON(){
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Dealer d) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_ID, d.getId());
			o.put(S_ERN, d.getErn());
			o.put(S_NAME, d.getName());
			o.put(S_URL_NAME, d.getUrlName());
			o.put(S_WEBSITE, d.getWebsite());
			o.put(S_LOGO, d.getLogo());
			o.put(S_COLOR, d.getColorString());
			o.put(S_PAGEFLIP, d.getPageflip().toJSON());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o; 
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

	public Dealer setColor(int color) {
		mColor = color;
		return this;
	}

	public int getColor() {
		return mColor;
	}
	
	public String getColorString() {
		return String.format("%06X", 0xFFFFFF & mColor);
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

	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("name=").append(mName)
		.append(", id=").append(mId);
		
		if (everything) {
			sb.append(", ern=").append(mErn)
			.append(", urlName=").append(mUrlName)
			.append(", website=").append(mWebsite)
			.append(", logo=").append(mLogo)
			.append(", color=").append(mColor)
			.append(", pageflip=").append(mPageflip.toString());
		}
		return sb.append("]").toString();
	}
}
