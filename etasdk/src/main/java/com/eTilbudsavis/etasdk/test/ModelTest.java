package com.eTilbudsavis.etasdk.test;

import android.os.Parcelable;

import com.eTilbudsavis.etasdk.Constants;
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
import com.eTilbudsavis.etasdk.utils.Utils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelTest {
	
	public static final String TAG = Constants.getTag(ModelTest.class);
	
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
		ShoppinglistItem obj = ModelCreator.getShoppinglistItem();
		ShoppinglistItem tmp = ModelCreator.getShoppinglistItem();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Parcelable
		testParcelable(obj, ShoppinglistItem.CREATOR);
        
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
		Shoppinglist obj = ModelCreator.getShoppinglist();
		Shoppinglist tmp = ModelCreator.getShoppinglist();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_SHOPPINGLIST, "12fakeid56");
		
		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Type can never be null (null == shoppinglist)
		Assert.assertNotNull(obj.getType());
		obj.setType(null);
		Assert.assertNotNull(obj.getType());
		Assert.assertEquals(Shoppinglist.TYPE_SHOPPING_LIST, obj.getType());
		
		// Parcelable
		testParcelable(obj, Shoppinglist.CREATOR);
        
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
		Session obj = ModelCreator.getSession();
		Session tmp = ModelCreator.getSession();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Session.CREATOR);
        
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
		Dealer obj = ModelCreator.getDealer();
		Dealer tmp = ModelCreator.getDealer();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_DEALER, "12fakeid56");
		
		// Parcelable
		testParcelable(obj, Dealer.CREATOR);
        
        obj.setColor(null);
		testParcelable(obj, Dealer.CREATOR);
        
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
		Catalog obj = ModelCreator.getCatalog();
		Catalog tmp = ModelCreator.getCatalog();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_CATALOG, "12fakeid56");
		
		// Parcelable
		testParcelable(obj, Catalog.CREATOR);
        
        obj.setBackground(null);
		testParcelable(obj, Catalog.CREATOR);
        
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
        
        EtaSdkTest.logTestWarning(TAG, "Catalog", "NO TEST OF HOTSPOTMAP, see HotspotMap test");
        
	}

	public static void testStore() {
		Store obj = ModelCreator.getStore();
		Store tmp = ModelCreator.getStore();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_STORE, "12fakeid56");
		
		// Parcelable
		testParcelable(obj, Store.CREATOR);
        
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
		Country obj = ModelCreator.getCountry();
		Country tmp = ModelCreator.getCountry();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_COUNTRY, "EU");
		
		// Parcelable
		testParcelable(obj, Country.CREATOR);
        
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
		Share obj = ModelCreator.getShare();
		Share tmp = ModelCreator.getShare();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// They must be the 'same' despite state, but not 'equal'
		obj.setState(SyncState.SYNCED);
		tmp.setState(SyncState.DELETE);
		Assert.assertNotSame(obj, tmp);
		Assert.assertTrue(obj.same(tmp));
		
		// Parcelable
		testParcelable(obj, Share.CREATOR);
        
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
		User obj = ModelCreator.getUser();
		User tmp = ModelCreator.getUser();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		String fakeUser = String.valueOf(User.NO_USER);
		String fakeErn = "ern:user:" + fakeUser;
		testIErn(obj, null, null, fakeUser, fakeErn);
		testIErn(obj, IErn.TYPE_USER, "1569");
		
		// Parcelable
		testParcelable(obj, User.CREATOR);
        
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
		Branding obj = ModelCreator.getBranding();
		Branding tmp = ModelCreator.getBranding();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Branding.CREATOR);
        
        obj.setColor(null);
		testParcelable(obj, Branding.CREATOR);
        
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
		Dimension obj = ModelCreator.getDimension();
		Dimension tmp = ModelCreator.getDimension();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Dimension.CREATOR);
        
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
		Hotspot obj = ModelCreator.getHotspot();
		Hotspot tmp = ModelCreator.getHotspot();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Hotspot.CREATOR);
        
        // TODO This contains json array JSON
        EtaSdkTest.logTestWarning(TAG, "Hotspot", "NO JSON TESTING DONE - Json will fail");
        
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
        
        EtaSdkTest.logTest(TAG, "Hotspot");
	}

	public static void testHotspotMap() {
		HotspotMap obj = ModelCreator.getHotspotMap();
		HotspotMap tmp = ModelCreator.getHotspotMap();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, HotspotMap.CREATOR);
        
        // TODO This contains json array JSON
        EtaSdkTest.logTestWarning(TAG, "HotspotMap", "NO JSON TESTING DONE - Json will fail");
//        JSONArray jObj = obj.toJSON();
//        Dimension d = ObjectCreator.getDimension();
//        HotspotMap jsonObj = HotspotMap.fromJSON(d, jObj);
//        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "HotspotMap");
        
	}

	public static void testPageflip() {
		Pageflip obj = ModelCreator.getPageflip();
		Pageflip tmp = ModelCreator.getPageflip();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Pageflip.CREATOR);
		
        obj.setColor(null);
		testParcelable(obj, Pageflip.CREATOR);
        
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
		
		Permission obj = ModelCreator.getPermission();
		Permission tmp = ModelCreator.getPermission();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Permission.CREATOR);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Permission jsonObj = Permission.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Permission");
        
	}

	public static void testTypeahead() {
		Typeahead obj = ModelCreator.getTypeahead();
		Typeahead tmp = ModelCreator.getTypeahead();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Typeahead.CREATOR);
        
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
		Subscription obj = ModelCreator.getSubscription();
		Subscription tmp = ModelCreator.getSubscription();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Subscription.CREATOR);
        
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
		Pricing obj = ModelCreator.getPricing();
		Pricing tmp = ModelCreator.getPricing();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Pricing.CREATOR);
        
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
		Links obj = ModelCreator.getLinks(id);
		Links tmp = ModelCreator.getLinks(id);
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Links.CREATOR);
        
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
		Images obj = ModelCreator.getImages(id);
		Images tmp = ModelCreator.getImages(id);
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// Parcelable
		testParcelable(obj, Images.CREATOR);
        
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
		Si obj = ModelCreator.getSi();
		Si tmp = ModelCreator.getSi();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// Parcelable
		testParcelable(obj, Si.CREATOR);
		
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
		Unit obj = ModelCreator.getUnit();
		Unit tmp = ModelCreator.getUnit();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// Parcelable
		testParcelable(obj, Unit.CREATOR);
        
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
		Pieces obj = ModelCreator.getPieces();
		Pieces tmp = ModelCreator.getPieces();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());
		
		// Parcelable
		testParcelable(obj, Pieces.CREATOR);
        
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
		Size obj = ModelCreator.getSize();
		Size tmp = ModelCreator.getSize();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// Parcelable
		testParcelable(obj, Size.CREATOR);
        
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
		Quantity obj = ModelCreator.getQuantity();
		Quantity tmp = ModelCreator.getQuantity();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		// Parcelable
		testParcelable(obj, Quantity.CREATOR);
        
        // JSON
        JSONObject jObj = obj.toJSON();
        Quantity jsonObj = Quantity.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        
        // getters and setters
        
        EtaSdkTest.logTest(TAG, "Quantity");
        
	}

	public static void testOffer() {
		
		// Test my generator
		Offer obj = ModelCreator.getOffer();
		Offer tmp = ModelCreator.getOffer();
		Assert.assertEquals(obj, tmp);
		Assert.assertEquals(obj.hashCode(), tmp.hashCode());

		testIErn(obj, null, null);
		testIErn(obj, IErn.TYPE_OFFER, "12fakeid56");
		
		// Parcelable
		testParcelable(obj, Offer.CREATOR);
        
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
        obj.setCatalog(ModelCreator.getCatalog());
        Assert.assertNotSame(catalogId, obj.getCatalogId());
        Assert.assertEquals(obj.getCatalogId(), obj.getCatalog().getId());
        obj.setCatalog(null);
        Assert.assertNull(obj.getCatalogId());
        
	}

	private static void testIStore(IStore<?> obj) {

        String storeId = obj.getStoreId();
        obj.setStore(ModelCreator.getStore());
        Assert.assertNotSame(storeId, obj.getStoreId());
        Assert.assertEquals(obj.getStoreId(), obj.getStore().getId());
        obj.setStore(null);
        Assert.assertNull(obj.getStoreId());
        
	}

	private static void testIDealer(IDealer<?> obj) {

        String dealerId = obj.getDealerId();
        obj.setDealer(ModelCreator.getDealer());
        Assert.assertNotSame(dealerId, obj.getDealerId());
        Assert.assertEquals(obj.getDealerId(), obj.getDealer().getId());
        obj.setDealer(null);
        Assert.assertNull(obj.getDealerId());
        
	}

	public static void testIErn(IErn<?> obj, String type, String fakeId) {
		testIErn(obj, type, fakeId, null, null);
	}
	
	public static void testIErn(IErn<?> obj, String type, String fakeId, String fakeExpectedId, String fakeExpectedErn) {
		
		String origId = obj.getId();
		if (origId!=null) {
			Assert.assertTrue(obj.getErn().startsWith("ern:"));
		} else {
			Assert.assertEquals(null, obj.getErn());
		}
		
		// The type must always exist
		Assert.assertNotNull(obj.getErnType());
		if (type!=null) {
			Assert.assertEquals(type, obj.getErnType());
		} else {
			Assert.assertNotSame(type, obj.getErnType());
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

	public static <T extends Parcelable> void testParcelable(T obj, Parcelable.Creator<T> creator) {
		T copy = Utils.copyParcelable(obj, creator);
		// May not refer to the same object
		Assert.assertNotSame(obj, copy);
		// Must be equal
		Assert.assertEquals(obj, copy);
	}
	
}
