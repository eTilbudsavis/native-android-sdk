package com.eTilbudsavis.etasdk.model.interfaces;

import com.eTilbudsavis.etasdk.model.Store;

public interface IStore<T> {

    public Store getStore();

    public String getStoreId();

    public T setStore(Store s);

}
