package com.eTilbudsavis.etasdk.EtaObjects.Interface;

import com.eTilbudsavis.etasdk.EtaObjects.Dealer;

public interface IDealer<T> {
	
	public Dealer getDealer();
	public String getDealerId();
	public T setDealer(Dealer d);
	
}
