package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.eTilbudsavis.etasdk.model.Branding;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Country;
import com.eTilbudsavis.etasdk.model.Dealer;
import com.eTilbudsavis.etasdk.model.Dimension;
import com.eTilbudsavis.etasdk.model.Hotspot;
import com.eTilbudsavis.etasdk.model.HotspotMap;
import com.eTilbudsavis.etasdk.model.Images;
import com.eTilbudsavis.etasdk.model.Links;
import com.eTilbudsavis.etasdk.model.Offer;
import com.eTilbudsavis.etasdk.model.Pageflip;
import com.eTilbudsavis.etasdk.model.Permission;
import com.eTilbudsavis.etasdk.model.Pieces;
import com.eTilbudsavis.etasdk.model.Pricing;
import com.eTilbudsavis.etasdk.model.Quantity;
import com.eTilbudsavis.etasdk.model.Session;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.Si;
import com.eTilbudsavis.etasdk.model.Size;
import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.model.Subscription;
import com.eTilbudsavis.etasdk.model.Typeahead;
import com.eTilbudsavis.etasdk.model.Unit;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.ICatalog;
import com.eTilbudsavis.etasdk.model.interfaces.IDealer;
import com.eTilbudsavis.etasdk.model.interfaces.IErn;
import com.eTilbudsavis.etasdk.model.interfaces.IStore;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.Api.JsonKey;

public class ObjectTest {
	
	public static final String TAG = ObjectTest.class.getSimpleName();
	
	public static void test() {
		
		EtaSdkTest.start(TAG);
		
		// The first set of objects, have no dependencies to other objects
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
		testCountry();
		
		// The following have dependencies to other ETA classes - order is important to make life easier
		testHotspotMap();
		testQuantity();
		testBranding();
		testShare();
		testUser();
		testStore();
		testCatalog();
		testDealer();
		testSession();
		testShoppinglist();
		testShoppinglistitem();
		testOffer();
	}

	public static void testShoppinglistitem() {
		ShoppinglistItem obj = ObjectCreator.getShoppinglistItem();
		ShoppinglistItem tmp = ObjectCreator.getShoppinglistItem();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ShoppinglistItem parceledObj = ShoppinglistItem.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        ShoppinglistItem jsonObj = ShoppinglistItem.fromJSON(jObj);
        
        Assert.assertNotSame(obj, jsonObj);
        Assert.assertTrue(obj.same(jsonObj));
        
        try {
            jObj.put(JsonKey.DESCRIPTION, "not-pizza");
        } catch (JSONException e) {
        }
        jsonObj = ShoppinglistItem.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "ShoppinglistItem");
        
	}

	public static void testShoppinglist() {
		Shoppinglist obj = ObjectCreator.getShoppinglist();
		Shoppinglist tmp = ObjectCreator.getShoppinglist();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null);
		testIErn(obj, "12fakeid56");
		
		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Shoppinglist parceledObj = Shoppinglist.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Shoppinglist jsonObj = Shoppinglist.fromJSON(jObj);
        
        Assert.assertNotSame(obj, jsonObj);
        Assert.assertTrue(obj.same(jsonObj));
        
        try {
            jObj.put(JsonKey.NAME, "not bents list anymore");
        } catch (JSONException e) {
        }
        jsonObj = Shoppinglist.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Shoppinglist");
        
	}

	public static void testSession() {
		Session obj = ObjectCreator.getSession();
		Session tmp = ObjectCreator.getSession();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Session parceledObj = Session.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Session jsonObj = Session.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        
        try {
            jObj.put(JsonKey.TOKEN, "new-fake-token");
        } catch (JSONException e) {
        }
        jsonObj = Session.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Session");
        
	}

	public static void testDealer() {
		Dealer obj = ObjectCreator.getDealer();
		Dealer tmp = ObjectCreator.getDealer();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null);
		testIErn(obj, "12fakeid56");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Dealer parceledObj = Dealer.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        obj.setColor(null);
        parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        parceledObj = Dealer.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Dealer jsonObj = Dealer.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.NAME, "bentes frisør salon");
        } catch (JSONException e) {
        }
        jsonObj = Dealer.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Dealer");
        
	}
	
	public static void testCatalog() {
		Catalog obj = ObjectCreator.getCatalog();
		Catalog tmp = ObjectCreator.getCatalog();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		testIErn(obj, null);
		testIErn(obj, "12fakeid56");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Catalog parceledObj = Catalog.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        obj.setBackground(null);
        parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        parceledObj = Catalog.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Catalog jsonObj = Catalog.fromJSON(jObj);
        
        // TODO Fix HotspotMap - then remove this
        obj.setHotspots(null);
        
        Assert.assertEquals(obj, jsonObj);
        
        try {
            jObj.put(JsonKey.OFFER_COUNT, 0);
        } catch (JSONException e) {
        }
        jsonObj = Catalog.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters

        testIDealer(obj);
        testIStore(obj);
        
        EtaSdkTest.logTest(TAG, "Catalog - NO TEST OF HOTSPOTMAP");
        
	}

	public static void testStore() {
		Store obj = ObjectCreator.getStore();
		Store tmp = ObjectCreator.getStore();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null);
		testIErn(obj, "12fakeid56");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Store parceledObj = Store.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Store jsonObj = Store.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.STREET, "fake-street-new");
        } catch (JSONException e) {
        }
        jsonObj = Store.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        testIDealer(obj);
        
        EtaSdkTest.logTest(TAG, "Store");
        
	}

	public static void testCountry() {
		Country obj = ObjectCreator.getCountry();
		Country tmp = ObjectCreator.getCountry();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null);
		testIErn(obj, "EU");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Country parceledObj = Country.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Country jsonObj = Country.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.ID, "US");
        } catch (JSONException e) {
        }
        jsonObj = Country.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Country");
        
	}

	public static void testShare() {
		Share obj = ObjectCreator.getShare();
		Share tmp = ObjectCreator.getShare();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Share parceledObj = Share.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Share jsonObj = Share.fromJSON(jObj);
        try {
            jObj.put(JsonKey.EMAIL, "fake-wrong-email@nomail.org");
        } catch (JSONException e) {
        }
        jsonObj = Share.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Share");
        
	}

	public static void testUser() {
		User obj = ObjectCreator.getUser();
		User tmp = ObjectCreator.getUser();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		String fakeUser = String.valueOf(User.NO_USER);
		String fakeErn = "ern:user:" + fakeUser;
		testIErn(obj, null, fakeUser, fakeErn);
		testIErn(obj, "1569");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        User parceledObj = User.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        User jsonObj = User.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKey.GENDER, "male");
        } catch (JSONException e) {
        }
        jsonObj = User.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "User");
        
	}

	public static void testBranding() {
		Branding obj = ObjectCreator.getBranding();
		Branding tmp = ObjectCreator.getBranding();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Branding parceledObj = Branding.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        obj.setColor(null);
        obj.setLogoBackground(null);
        parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        parceledObj = Branding.CREATOR.createFromParcel(parcel);
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
        
        EtaSdkTest.logTest(TAG, "Branding");
        
	}

	public static void testDimension() {
		Dimension obj = ObjectCreator.getDimension();
		Dimension tmp = ObjectCreator.getDimension();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Dimension");
        
	}

	public static void testHotspot() {
		Hotspot obj = ObjectCreator.getHotspot();
		Hotspot tmp = ObjectCreator.getHotspot();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Hotspot - NO JSON TESTING DONE");
        
	}

	public static void testHotspotMap() {
		HotspotMap obj = ObjectCreator.getHotspotMap();
		HotspotMap tmp = ObjectCreator.getHotspotMap();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        HotspotMap parceledObj = HotspotMap.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);
        
        // TODO This contains json array JSON
//        JSONArray jObj = obj.toJSON();
//        Dimension d = ObjectCreator.getDimension();
//        HotspotMap jsonObj = HotspotMap.fromJSON(d, jObj);
//        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "HotspotMap - NO JSON TESTING DONE");
        
	}

	public static void testPageflip() {
		Pageflip obj = ObjectCreator.getPageflip();
		Pageflip tmp = ObjectCreator.getPageflip();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Pageflip parceledObj = Pageflip.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledObj);

        obj.setColor(null);
        parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        parceledObj = Pageflip.CREATOR.createFromParcel(parcel);
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
        
        EtaSdkTest.logTest(TAG, "Pageflip");
        
	}

	public static void testPermission() {
		
		Permission obj = ObjectCreator.getPermission();
		Permission tmp = ObjectCreator.getPermission();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Permission");
        
	}

	public static void testTypeahead() {
		Typeahead obj = ObjectCreator.getTypeahead();
		Typeahead tmp = ObjectCreator.getTypeahead();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Typeahead");
        
	}

	public static void testSubscription() {
		Subscription obj = ObjectCreator.getSubscription();
		Subscription tmp = ObjectCreator.getSubscription();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Subscription");
        
	}

	public static void testPricing() {
		Pricing obj = ObjectCreator.getPricing();
		Pricing tmp = ObjectCreator.getPricing();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Pricing");
        
	}

	public static void testLinks() {
		String id = "fake-id";
		Links obj = ObjectCreator.getLinks(id);
		Links tmp = ObjectCreator.getLinks(id);
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Links");
        
	}

	public static void testImages() {
		String id = "fake-id";
		Images obj = ObjectCreator.getImages(id);
		Images tmp = ObjectCreator.getImages(id);
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

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
        
        EtaSdkTest.logTest(TAG, "Images");
        
	}

	public static void testSi() {
		Si obj = ObjectCreator.getSi();
		Si tmp = ObjectCreator.getSi();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

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
        
        EtaSdkTest.logTest(TAG, "Si");
        
	}

	public static void testUnit() {
		Unit obj = ObjectCreator.getUnit();
		Unit tmp = ObjectCreator.getUnit();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

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
        
        EtaSdkTest.logTest(TAG, "Unit");
        
	}

	public static void testPieces() {
		Pieces obj = ObjectCreator.getPieces();
		Pieces tmp = ObjectCreator.getPieces();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
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
        
        EtaSdkTest.logTest(TAG, "Pieces");
        
	}

	public static void testSize() {
		Size obj = ObjectCreator.getSize();
		Size tmp = ObjectCreator.getSize();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

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
        
        EtaSdkTest.logTest(TAG, "Size");
        
	}

	public static void testQuantity() {
		Quantity obj = ObjectCreator.getQuantity();
		Quantity tmp = ObjectCreator.getQuantity();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

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
        
        EtaSdkTest.logTest(TAG, "Quantity");
        
	}

	public static void testOffer() {
		
		// Test my generator
		Offer obj = ObjectCreator.getOffer();
		Offer tmp = ObjectCreator.getOffer();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null);
		testIErn(obj, "12fakeid56");
		
		// Parcelable
        Parcel parcel = Parcel.obtain();
        obj.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Offer parceledobj = Offer.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(obj, parceledobj);
        
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
        JSONObject jOffer = obj.toJSON();
        Offer jsonObj = Offer.fromJSON(jOffer);
        Assert.assertEquals(obj, jsonObj);
        try {
            jOffer.put(JsonKey.HEADING, "Not an offer heading");
        } catch (JSONException e) {
        }
        jsonObj = Offer.fromJSON(jOffer);
        Assert.assertNotSame(obj, jsonObj);
        
        testIDealer(obj);
        testIStore(obj);
        testICatalog(obj);
        
        EtaSdkTest.logTest(TAG, "Offer");
        // getters and setters
		
    }
	
	
	/*
	 * 
	 * HELPERS TO TEST OBJECTS THAT IMPLEMENT CERTAIN TYPES OC INTERFACES
	 * 
	 */
	
	
	private static void testICatalog(ICatalog<?> obj) {

        String catalogId = obj.getCatalogId();
        obj.setCatalog(ObjectCreator.getCatalog());
        Assert.assertNotSame(catalogId, obj.getCatalogId());
        Assert.assertEquals(obj.getCatalogId(), obj.getCatalog().getId());
        obj.setCatalog(null);
        Assert.assertNull(obj.getCatalogId());
        
	}

	private static void testIStore(IStore<?> obj) {

        String storeId = obj.getStoreId();
        obj.setStore(ObjectCreator.getStore());
        Assert.assertNotSame(storeId, obj.getStoreId());
        Assert.assertEquals(obj.getStoreId(), obj.getStore().getId());
        obj.setStore(null);
        Assert.assertNull(obj.getStoreId());
        
	}

	private static void testIDealer(IDealer<?> obj) {

        String dealerId = obj.getDealerId();
        obj.setDealer(ObjectCreator.getDealer());
        Assert.assertNotSame(dealerId, obj.getDealerId());
        Assert.assertEquals(obj.getDealerId(), obj.getDealer().getId());
        obj.setDealer(null);
        Assert.assertNull(obj.getDealerId());
        
	}

	public static void testIErn(IErn<?> obj, String fakeId) {
		testIErn(obj, fakeId, null, null);
	}
	
	public static void testIErn(IErn<?> obj, String fakeId, String fakeExpectedId, String fakeExpectedErn) {
		
		String origId = obj.getId();
		if (origId!=null) {
			Assert.assertTrue(obj.getErn().startsWith("ern:"));
			// if there is an id, there must also be a type (or the id)
			Assert.assertNotNull(obj.getErnType());
		} else {
			Assert.assertEquals(null, obj.getErn());
		}
		
		if (fakeId == null) {
			obj.setId(null);
			Assert.assertEquals(fakeExpectedId, obj.getId());
			Assert.assertEquals(fakeExpectedErn, obj.getErn());
		} else {
			obj.setId(fakeId);
			Assert.assertTrue(obj.getErn().startsWith("ern:"));
			Assert.assertTrue(obj.getErn().contains(fakeId));
			Assert.assertTrue(obj.getErn().contains(obj.getErnType()));
		}
		
		obj.setId(origId);
		
	}

}
