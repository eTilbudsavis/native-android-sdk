package com.eTilbudsavis.etasdk.model.interfaces;

import com.eTilbudsavis.etasdk.model.Catalog;

public interface ICatalog<T> {

    public Catalog getCatalog();

    public String getCatalogId();

    public T setCatalog(Catalog c);

}
