package com.eTilbudsavis.etasdk.EtaObjects.Interface;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public interface ICatalog<T> {
	
	public Catalog getCatalog();
	public String getCatalogId();
	public T setCatalog(Catalog c);
	
}
