/**
 * <b>DO NOT USE THIS PACKAGE</b>
 * 
 * <br/><br/>
 * 
 * This package is broken, and not yet ready for production. It will fail!
 * 
 * <br/><br/>
 * 
 * The classes and methods, in this package is intended for performing multiple requests against the eTilbudsavis API.
 * It's designed to be easy to use, and wrap some queries to make life easier.
 * 
 * <br/><br/>
 * 
 * There will be helper classes, indented for filling model objects into a requested resource. Given the resource has
 * id's to other resources. E.g.: When requesting a {@link com.eTilbudsavis.etasdk.model.Catalog Catalog}, 
 * you can request to have both the {@link com.eTilbudsavis.etasdk.model.Store Store}, and {@link com.eTilbudsavis.etasdk.model.Dealer Dealer}
 * object automatically filled in to the {@link com.eTilbudsavis.etasdk.model.Catalog Catalog}, instead of just having references to id's.
 * 
 */
package com.eTilbudsavis.etasdk.request;