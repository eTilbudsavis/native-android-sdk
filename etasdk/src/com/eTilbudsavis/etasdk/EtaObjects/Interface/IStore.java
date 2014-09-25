package com.eTilbudsavis.etasdk.EtaObjects.Interface;

import com.eTilbudsavis.etasdk.EtaObjects.Store;

public interface IStore<T> {
	
	public Store getStore();
	public String getStoreId();
	public T setStore(Store s);
	
}
