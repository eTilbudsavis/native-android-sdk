package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Branding;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Dimension;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.EtaObjects.helper.HotspotMap;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Images;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Links;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pageflip;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Permission;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pieces;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pricing;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Quantity;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Si;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Size;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Subscription;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Typeahead;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Unit;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;

public class ObjectTest {
	
	public static final String TAG = ObjectTest.class.getSimpleName();
	
	public static void test() {
		testSi();
		testSize();
		testPieces();
		testImages();
		testLinks();
		testUnit();
		testPricing();
		testTypeahead();
		testSubscription();
		testPermission();
		testPageflip();
		testHotspot();
		testDimension();
		// The following have dependencies to other eta classes - they run last
		testHotspotMap();
		testQuantity();
		testBranding();
		testOffer();
	}

	public static void testBranding() {
		Branding obj = ObjectCreator.getBranding();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Branding parceledObj = Branding.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Branding jsonObj = Branding.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.NAME, "fake-branding-name-new");
        } catch (JSONException e) {
        }
        jsonObj = Branding.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Branding");
        
	}

	public static void testDimension() {
		Dimension obj = ObjectCreator.getDimension();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Dimension parceledObj = Dimension.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Dimension jsonObj = Dimension.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.HEIGHT, 2.0d);
        } catch (JSONException e) {
        }
        jsonObj = Dimension.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Dimension");
        
	}

	public static void testHotspot() {
		Hotspot obj = ObjectCreator.getHotspot();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Hotspot parceledObj = Hotspot.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // TODO This contains json array JSON
//        JSONObject jObj = obj.toJSON();
//        Hotspot jsonObj = Hotspot.fromJSON(jObj);
//        Assert.assertEquals(obj, jsonObj);
//        try {
//            jObj.put(JsonKey.LOGO, "fake-logo-url-new");
//        } catch (JSONException e) {
//        }
//        jsonObj = Hotspot.fromJSON(jObj);
//        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Hotspot - NEEDS JSON STUFF");
        
	}

	public static void testHotspotMap() {
		HotspotMap obj = ObjectCreator.getHotspotMap();
		
		// TODO HotspotMap is broken, it needs to be fixed
		
		// Parcelable
//        Parcel parcel = Parcel.obtain();
//        obj.writeToParcel(parcel, 0);
//        parcel.setDataPosition(0);
//        HotspotMap parceledObj = HotspotMap.CREATOR.createFromParcel(parcel);
//        Assert.assertEquals(obj, parceledObj);
        
        // TODO This contains json array JSON
//        JSONObject jObj = obj.toJSON();
//        HotspotMap jsonObj = HotspotMap.fromJSON(jObj);
//        Assert.assertEquals(obj, jsonObj);
//        try {
//            jObj.put(JsonKey.LOGO, "fake-logo-url-new");
//        } catch (JSONException e) {
//        }
//        jsonObj = HotspotMap.fromJSON(jObj);
//        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "HotspotMap - NO TESTING DONE");
        
	}

	public static void testPageflip() {
		Pageflip obj = ObjectCreator.getPageflip();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Pageflip parceledObj = Pageflip.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Pageflip jsonObj = Pageflip.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.LOGO, "fake-logo-url-new");
        } catch (JSONException e) {
        }
        jsonObj = Pageflip.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Pageflip");
        
	}

	public static void testPermission() {
		
		Permission obj = ObjectCreator.getPermission();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Permission parceledObj = Permission.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Permission jsonObj = Permission.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Permission");
        
	}

	public static void testTypeahead() {
		Typeahead obj = ObjectCreator.getTypeahead();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Typeahead parceledObj = Typeahead.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Typeahead jsonObj = Typeahead.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.SUBJECT, "fake-subject-new");
        } catch (JSONException e) {
        }
        jsonObj = Typeahead.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Typeahead");
        
	}

	public static void testSubscription() {
		Subscription obj = ObjectCreator.getSubscription();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Subscription parceledObj = Subscription.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Subscription jsonObj = Subscription.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.DEALER_ID, "fake-dealer-new");
        } catch (JSONException e) {
        }
        jsonObj = Subscription.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Subscription");
        
	}

	public static void testPricing() {
		Pricing obj = ObjectCreator.getPricing();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Pricing parceledObj = Pricing.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Pricing jsonObj = Pricing.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.PRICE, Double.MAX_VALUE);
        } catch (JSONException e) {
        }
        jsonObj = Pricing.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Pricing");
        
	}

	public static void testLinks() {
		Links obj = ObjectCreator.getLinks("fake-id");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Links parceledObj = Links.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Links jsonObj = Links.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.WEBSHOP, "not fake-id");
        } catch (JSONException e) {
        }
        jsonObj = Links.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Links");
        
	}

	public static void testImages() {
		Images obj = ObjectCreator.getImages("fake-id");

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Images parceledObj = Images.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Images jsonObj = Images.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.VIEW, "not fake-id");
        } catch (JSONException e) {
        }
        jsonObj = Images.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Images");
        
	}

	public static void testSi() {
		Si obj = ObjectCreator.getSi();

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Si parceledObj = Si.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
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
        
        EtaSdkTest.log(TAG, "Si");
        
	}

	public static void testUnit() {
		Unit obj = ObjectCreator.getUnit();

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Unit parceledObj = Unit.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Unit jsonObj = Unit.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.SYMBOL, "not foobar");
        } catch (JSONException e) {
        }
        jsonObj = Unit.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Unit");
        
	}

	public static void testPieces() {
		Pieces obj = ObjectCreator.getPieces();
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Pieces parceledObj = Pieces.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Pieces jsonObj = Pieces.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.TO, "4.0");
        } catch (JSONException e) {
        }
        jsonObj = Pieces.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Pieces");
        
	}

	public static void testSize() {
		Size obj = ObjectCreator.getSize();

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Size parceledObj = Size.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Size jsonObj = Size.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.TO, "4.0");
        } catch (JSONException e) {
        }
        jsonObj = Size.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Size");
        
	}

	public static void testQuantity() {
		Quantity obj = ObjectCreator.getQuantity();

		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Quantity parceledObj = Quantity.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Quantity jsonObj = Quantity.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.log(TAG, "Quantity");
        
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
        
        EtaSdkTest.log(TAG, "Offer");
        // getters and setters
		
    }
	
}
