package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Si;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;

public class ObjectTest {
	
	public static final String TAG = ObjectTest.class.getSimpleName();
	
	public static void test() {
		testOffer();
	}

	public static void testSi() {
		Si obj = ObjectCreator.getSi();

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Si parceledObj = Si.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        Assert.assertEquals(obj.hashCode(), parceledObj.hashCode());
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Si jsonObj = Si.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.SYMBOL, "not foobar");
        } catch (JSONException e) {
        }
        jsonObj = Si.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaLog.d(TAG, "Si");
        
	}
	
	public static void testOffer() {
		
		// Test my generator
		Offer offer = ObjectCreator.getOffer();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        offer.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Offer parceledOffer = Offer.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(offer, parceledOffer);
        Assert.assertEquals(offer.hashCode(), parceledOffer.hashCode());

        // TODO Can't run thing yet, uncomment when moved to junit project
		// Parcelable - false
//        parcel = Parcel.obtain();
//        offer.writeToParcel(parcel, 0);
//        parcel.setDataPosition(0);
//        parcel.readString(); // This should make it impossible to recreate the offer
//        parceledOffer = Offer.CREATOR.createFromParcel(parcel);
//        Assert.assertNotSame(offer, parceledOffer);
//        Assert.assertEquals(offer.hashCode(), parceledOffer.hashCode());
        
        // JSON
        JSONObject jOffer = offer.toJSON();
        Offer jsonOffer = Offer.fromJSON(jOffer);
        Assert.assertEquals(offer, jsonOffer);
        try {
            jOffer.put(JsonKey.HEADING, "Not an offer heading");
        } catch (JSONException e) {
        }
        jsonOffer = Offer.fromJSON(jOffer);
        Assert.assertNotSame(offer, jsonOffer);
        
        EtaLog.d(TAG, "Offer");
        // getters and setters
		
    }
	
}
