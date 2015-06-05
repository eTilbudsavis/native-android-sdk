package com.eTilbudsavis.etasdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.interfaces.IJson;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Subscription implements IJson<JSONObject>, Parcelable {
	
	public static final String TAG = Constants.getTag(Subscription.class);

	private Dealer mDealer;
	private String mDealerId;
	private boolean mSubscribed = false;
	
	private Subscription() {
		// empty
	}
	
	public Subscription(Subscription s) {
		setDealer(s.getDealer());
		setDealerId(s.getDealerId());
		setSubscribed(s.isSubscribed());
	}

	public Subscription(Dealer d) {
		setDealer(d);
	}

	public Subscription(String dealerId) {
		setDealerId(dealerId);
	}
	
	public static Subscription fromJSON(JSONObject subscription) {
		Subscription s = new Subscription();
		if (subscription == null) {
			return s;
		}
		
		s.setDealerId(Json.valueOf(subscription, JsonKey.DEALER_ID));
		s.setSubscribed(Json.valueOf(subscription, JsonKey.SUBSCRIBED, false));
		
		if (subscription.has(JsonKey.SDK_DEALER)) {
			s.setDealer(Dealer.fromJSON(Json.getObject(subscription, JsonKey.SDK_DEALER)));
		}
		
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
			
			if (mDealer!=null) {
				o.put(JsonKey.SDK_DEALER, Json.toJson(mDealer));
			}
			
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return o;
	}

	public void setDealer(Dealer d) {
		mDealer = d;
		if (mDealer != null) {
			setDealerId(mDealer.getId());
		}
	}
	
	public Dealer getDealer() {
		return mDealer;
	}
	
	public String getDealerId() {
		return mDealerId;
	}
	
	public void setDealerId(String dealerId) {
		mDealerId = dealerId;
		if (mDealer != null && !mDealer.getId().equals(mDealerId)) {
			setDealer(null);
		}
	}
	
	public boolean isSubscribed() {
		return mSubscribed;
	}

	public void setSubscribed(boolean subscribed) {
		mSubscribed = subscribed;
	}
	
	public void toggle() {
		mSubscribed = !mSubscribed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mDealer == null) ? 0 : mDealer.hashCode());
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
		if (mDealer == null) {
			if (other.mDealer != null)
				return false;
		} else if (!mDealer.equals(other.mDealer))
			return false;
		if (mDealerId == null) {
			if (other.mDealerId != null)
				return false;
		} else if (!mDealerId.equals(other.mDealerId))
			return false;
		if (mSubscribed != other.mSubscribed)
			return false;
		return true;
	}
	
	
	
	/**
	 * Compare object, that uses {@link Dealer#getName() name} to compare two lists.
	 */
	public static Comparator<Subscription> DEALER_NAME_COMPARATOR  = new Comparator<Subscription>() {

		public int compare(Subscription item1, Subscription item2) {
			
			if (item1 == null || item2 == null) {
				return item1 == null ? (item2 == null ? 0 : 1) : -1;
			}

			Dealer d1 = item1.getDealer();
			Dealer d2 = item2.getDealer();
			if (d1 == null || d2 == null) {
				return d1 == null ? (d2 == null ? 0 : 1) : -1;
			} else {
				String t1 = d1.getName();
				String t2 = d2.getName();
				if (t1 == null || t2 == null) {
					return t1 == null ? (t2 == null ? 0 : 1) : -1;
				}
				
				//ascending order
				return t1.compareToIgnoreCase(t2);
			}
			
		}

	};
	
	public String toString() {
		JSONObject o = toJSON();
		o.remove(JsonKey.SDK_DEALER);
		if (mDealer != null) {
			try {
				o.put(JsonKey.SDK_DEALER, mDealer.getName());
			} catch (JSONException e) {
			}
		}
		return o.toString();
	};
	
	public static Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>(){
		public Subscription createFromParcel(Parcel source) {
			return new Subscription(source);
		}
		public Subscription[] newArray(int size) {
			return new Subscription[size];
		}
	};

	private Subscription(Parcel in) {
		this.mDealer = in.readParcelable(Dealer.class.getClassLoader());
		this.mDealerId = in.readString();
		this.mSubscribed = in.readByte() != 0;
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(this.mDealer, flags);
		dest.writeString(this.mDealerId);
		dest.writeByte(mSubscribed ? (byte) 1 : (byte) 0);
	}

}
