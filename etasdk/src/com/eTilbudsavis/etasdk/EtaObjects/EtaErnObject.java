package com.eTilbudsavis.etasdk.EtaObjects;



/**
 * EtaErnObject is an object, is one of the objects which can which JSON representation can be easily 
 * identified by the 'ern' key. 
 * @author oizo
 *
 */
public abstract class EtaErnObject<T> extends EtaObject {

	public static final String TAG = "EtaErnObject";

	protected static final String ERN_CATALOG = "ern:catalog";
	protected static final String ERN_DEALER = "ern:dealer";
	protected static final String ERN_OFFER = "ern:offer";
	protected static final String ERN_SHOPPINGLIST = "ern:shopping:list";
	protected static final String ERN_SHOPPINGLISTITEM = "ern:shoppinglist:item";
	protected static final String ERN_STORE = "ern:store";
	
	private String mId;
	private String mErn;
	
	public EtaErnObject() {
		
	}
	
	public abstract String getErnPrefix();
	
	@SuppressWarnings("unchecked")
	public T setId(String id) {
		mId = id;
		mErn = getErnPrefix() + ":" + id;
		return (T)this;
	}
	
	public String getId() {
		return mId;
	}
	
	@SuppressWarnings("unchecked")
	public T setErn(String ern) {
		mErn = ern;
		String[] parts = mErn.split(":");
		mId = parts[parts.length-1];
		return (T)this;
	}
	
	public String getErn() {
		return mErn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
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
		EtaErnObject<?> other = (EtaErnObject<?>) obj;
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
		return true;
	}
	
}
