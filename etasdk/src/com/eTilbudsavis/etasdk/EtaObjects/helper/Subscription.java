package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;
import android.os.Parcelable;
import android.os.Parcel;

public class Subscription implements EtaObject<JSONObject>, Serializable, Parcelable {
	
	public static final String TAG = Subscription.class.getSimpleName();
	
	private static final long serialVersionUID = 1548862774286265086L;
	
	private String mDealerId;
	private boolean mSubscribed = false;

	public static Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>(){
		public Subscription createFromParcel(Parcel source) {
			return new Subscription(source);
		}
		public Subscription[] newArray(int size) {
			return new Subscription[size];
		}
	};
	
	public Subscription() {
		
	}
	
	public static Subscription fromJSON(JSONObject subscription) {
		Subscription s = new Subscription();
		if (subscription == null) {
			return s;
		}
		
		s.setDealerId(Json.valueOf(subscription, JsonKey.DEALER_ID));
		s.setSubscribed(Json.valueOf(subscription, JsonKey.SUBSCRIBED, false));
		
		return s;
	}

	public static List<Subscription> fromJSON(JSONArray subscription) {
		List<Subscription> list = new ArrayList<Subscription>();
		try {
			for (int i = 0 ; i < subscription.length() ; i++ ) {
				list.add(Subscription.fromJSON((JSONObject) subscription.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return list;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.DEALER_ID, Json.nullCheck(mDealerId));
			o.put(JsonKey.SUBSCRIBED, mSubscribed);
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return o;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public void setDealerId(String dealerId) {
		mDealerId = dealerId;
	}

	public boolean isSubscribed() {
		return mSubscribed;
	}

	public void setSubscribed(boolean subscribed) {
		mSubscribed = subscribed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result + (mSubscribed ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subscription other = (Subscription) obj;
		if (mDealerId == null) {
			if (other.mDealerId != null)
				return false;
		} else if (!mDealerId.equals(other.mDealerId))
			return false;
		if (mSubscribed != other.mSubscribed)
			return false;
		return true;
	}

	private Subscription(Parcel in) {
		this.mDealerId = in.readString();
		this.mSubscribed = in.readByte() != 0;
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mDealerId);
		dest.writeByte(mSubscribed ? (byte) 1 : (byte) 0);
	}
	
}
