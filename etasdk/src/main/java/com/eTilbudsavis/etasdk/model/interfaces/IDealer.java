package com.eTilbudsavis.etasdk.model.interfaces;

import com.eTilbudsavis.etasdk.model.Dealer;

public interface IDealer<T> {

    public Dealer getDealer();

    public String getDealerId();

    public T setDealer(Dealer d);

}
