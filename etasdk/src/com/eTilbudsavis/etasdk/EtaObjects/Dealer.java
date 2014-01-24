package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Dealer extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Dealer";
	
	private String mId;
	private String mErn;
	private String mName;
	private String mUrlName;
	private String mWebsite;
	private String mLogo;
	private Integer mColor;
	private Pageflip mPageflip;

	public Dealer() { }
	
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
	
	public static Dealer fromJSON(JSONObject dealer) {
		return fromJSON(new Dealer(), dealer);
	}
	
	private static Dealer fromJSON(Dealer d, JSONObject dealer) {
		if (d == null) d = new Dealer();
		if (dealer == null) return d;

		try {
			d.setId(jsonToString(dealer, ServerKey.ID));
			d.setErn(jsonToString(dealer, ServerKey.ERN));
			d.setName(jsonToString(dealer, ServerKey.NAME));
			d.setUrlName(jsonToString(dealer, ServerKey.URL_NAME));
			d.setWebsite(jsonToString(dealer, ServerKey.WEBSITE));
			d.setLogo(jsonToString(dealer, ServerKey.LOGO));
			d.setColor(Color.parseColor("#"+jsonToString(dealer, ServerKey.COLOR)));
			d.setPageflip(Pageflip.fromJSON(dealer.getJSONObject(ServerKey.PAGEFLIP)));
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
			o.put(ServerKey.ID, d.getId());
			o.put(ServerKey.ERN, d.getErn());
			o.put(ServerKey.NAME, d.getName());
			o.put(ServerKey.URL_NAME, d.getUrlName());
			o.put(ServerKey.WEBSITE, d.getWebsite());
			o.put(ServerKey.LOGO, d.getLogo());
			o.put(ServerKey.COLOR, d.getColorString());
			o.put(ServerKey.PAGEFLIP, d.getPageflip().toJSON());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o; 
	}
	
	public Dealer setId(String id) {
		this.mId = id;
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mColor == null) ? 0 : mColor.hashCode());
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result + ((mLogo == null) ? 0 : mLogo.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result
				+ ((mPageflip == null) ? 0 : mPageflip.hashCode());
		result = prime * result
				+ ((mUrlName == null) ? 0 : mUrlName.hashCode());
		result = prime * result
				+ ((mWebsite == null) ? 0 : mWebsite.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dealer other = (Dealer) obj;
		if (mColor == null) {
			if (other.mColor != null)
				return false;
		} else if (!mColor.equals(other.mColor))
			return false;
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
			return false;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		if (mLogo == null) {
			if (other.mLogo != null)
				return false;
		} else if (!mLogo.equals(other.mLogo))
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mPageflip == null) {
			if (other.mPageflip != null)
				return false;
		} else if (!mPageflip.equals(other.mPageflip))
			return false;
		if (mUrlName == null) {
			if (other.mUrlName != null)
				return false;
		} else if (!mUrlName.equals(other.mUrlName))
			return false;
		if (mWebsite == null) {
			if (other.mWebsite != null)
				return false;
		} else if (!mWebsite.equals(other.mWebsite))
			return false;
		return true;
	}
	
	
	
}
