package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.NetworkInterface.Request.Endpoint;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Sort;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Dealer extends EtaErnObject implements Serializable {
	
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
	public static final String FILTER_DEALER_IDS = Param.FILTER_DEALER_IDS;

	/** String identifying the query parameter */
	public static final String PARAM_QUERY = Param.QUERY;
	
	/** Endpoint for dealer list resource */
	public static final String ENDPOINT_LIST = Endpoint.DEALER_LIST;

	/** Endpoint for a single dealer resource */
	public static final String ENDPOINT_ID = Endpoint.DEALER_ID;

	/** Endpoint for searching dealers */
	public static final String ENDPOINT_SEARCH = Endpoint.DEALER_SEARCH;
	
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;

	public Dealer() { }
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Dealer> fromJSON(JSONArray dealers) {
		ArrayList<Dealer> list = new ArrayList<Dealer>();
		try {
			for (int i = 0 ; i < dealers.length() ; i++ )
				list.add(Dealer.fromJSON((JSONObject)dealers.get(i)));
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static Dealer fromJSON(JSONObject dealer) {
		return fromJSON(new Dealer(), dealer);
	}
	
	private static Dealer fromJSON(Dealer d, JSONObject dealer) {
		if (d == null) d = new Dealer();
		if (dealer == null) return d;

		try {
			d.setId(getJsonString(dealer, Key.ID));
			d.setErn(getJsonString(dealer, Key.ERN));
			d.setName(getJsonString(dealer, Key.NAME));
			d.setUrlName(getJsonString(dealer, Key.URL_NAME));
			d.setWebsite(getJsonString(dealer, Key.WEBSITE));
			d.setLogo(getJsonString(dealer, Key.LOGO));
			d.setColor(Color.parseColor("#"+getJsonString(dealer, Key.COLOR)));
			d.setPageflip(Pageflip.fromJSON(dealer.getJSONObject(Key.PAGEFLIP)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return d;
	}
	
	public JSONObject toJSON(){
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Dealer d) {
		JSONObject o = new JSONObject();
		try {
			o.put(Key.ID, d.getId());
			o.put(Key.ERN, d.getErn());
			o.put(Key.NAME, d.getName());
			o.put(Key.URL_NAME, d.getUrlName());
			o.put(Key.WEBSITE, d.getWebsite());
			o.put(Key.LOGO, d.getLogo());
			o.put(Key.COLOR, d.getColorString());
			o.put(Key.PAGEFLIP, d.getPageflip().toJSON());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o; 
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
		return stringCompare(mId, dealer.getId()) &&
				stringCompare(mErn, dealer.getErn()) &&
				stringCompare(mName, dealer.getName()) &&
				stringCompare(mUrlName, dealer.getUrlName()) &&
				stringCompare(mWebsite, dealer.getWebsite()) &&
				stringCompare(mLogo, dealer.getLogo()) &&
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
			.append(", pageflip=").append(mPageflip == null ? null : mPageflip.toString());
		}
		return sb.append("]").toString();
	}
}
