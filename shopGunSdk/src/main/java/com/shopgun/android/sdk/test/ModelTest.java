/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.test;

import android.os.Parcelable;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.api.JsonKeys;
import com.shopgun.android.sdk.model.Branding;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Country;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.model.Dimension;
import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.model.Links;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.model.Pageflip;
import com.shopgun.android.sdk.model.Permission;
import com.shopgun.android.sdk.model.Pieces;
import com.shopgun.android.sdk.model.Pricing;
import com.shopgun.android.sdk.model.Quantity;
import com.shopgun.android.sdk.model.Session;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.Si;
import com.shopgun.android.sdk.model.Size;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.Subscription;
import com.shopgun.android.sdk.model.Typeahead;
import com.shopgun.android.sdk.model.Unit;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.model.interfaces.SyncState;
import com.shopgun.android.sdk.utils.Utils;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

public class ModelTest {

    public static final String TAG = Constants.getTag(ModelTest.class);

    public static void test() {

        SdkTest.start(TAG);

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
        Assert.assertNull(ShoppinglistItem.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        ShoppinglistItem jsonObj = ShoppinglistItem.fromJSON(jObj);

        Assert.assertNotSame(obj, jsonObj);
        Assert.assertTrue(obj.same(jsonObj));

        try {
            jObj.put(JsonKeys.DESCRIPTION, "not-pizza");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = ShoppinglistItem.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "ShoppinglistItem");

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
        Assert.assertNull(Shoppinglist.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Shoppinglist jsonObj = Shoppinglist.fromJSON(jObj);

        Assert.assertNotSame(obj, jsonObj);
        Assert.assertTrue(obj.same(jsonObj));

        try {
            jObj.put(JsonKeys.NAME, "not bents list anymore");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Shoppinglist.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Shoppinglist");

    }

    public static void testSession() {
        Session obj = ModelCreator.getSession();
        Session tmp = ModelCreator.getSession();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Session.CREATOR);

        // JSON
        Assert.assertNull(Session.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Session jsonObj = Session.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);

        try {
            jObj.put(JsonKeys.TOKEN, "new-fake-token");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Session.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Session");

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
        Assert.assertNull(Dealer.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Dealer jsonObj = Dealer.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.NAME, "bentes frisør salon");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Dealer.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Dealer");

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
        Assert.assertNull(Catalog.fromJSON((JSONObject) null));
        JSONObject jObj = obj.toJSON();
        Catalog jsonObj = Catalog.fromJSON(jObj);

        // TODO Fix HotspotMap - then remove this
        obj.setHotspots(null);

        Assert.assertEquals(obj, jsonObj);

        try {
            jObj.put(JsonKeys.OFFER_COUNT, 0);
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Catalog.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        testIDealer(obj);
        testIStore(obj);

        SdkTest.logTestWarning(TAG, "Catalog", "NO TEST OF HOTSPOTMAP, see HotspotMap test");

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
        Assert.assertNull(Store.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Store jsonObj = Store.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.STREET, "fake-street-new");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Store.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters
        testIDealer(obj);

        SdkTest.logTest(TAG, "Store");

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
        Assert.assertNull(Country.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Country jsonObj = Country.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.ID, "US");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Country.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Country");

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
        Assert.assertNull(Share.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        try {
            jObj.put(JsonKeys.EMAIL, "fake-wrong-email@nomail.org");
        } catch (JSONException e) {
            // ignore
        }
        Share jsonObj = Share.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Share");

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
        Assert.assertNull(User.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        User jsonObj = User.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.GENDER, "male");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = User.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "User");

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
        Assert.assertNull(Branding.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Branding jsonObj = Branding.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.NAME, "fake-branding-name-new");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Branding.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Branding");

    }

    public static void testDimension() {
        Dimension obj = ModelCreator.getDimension();
        Dimension tmp = ModelCreator.getDimension();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Dimension.CREATOR);

        // JSON
        Assert.assertNull(Dimension.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Dimension jsonObj = Dimension.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.HEIGHT, 2.0d);
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Dimension.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Dimension");

    }

    public static void testHotspot() {
        Hotspot obj = ModelCreator.getHotspot();
        Hotspot tmp = ModelCreator.getHotspot();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Hotspot.CREATOR);

        // TODO This contains json array JSON
        SdkTest.logTestWarning(TAG, "Hotspot", "NO JSON TESTING DONE - Json will fail");

//        JSONObject jObj = obj.toJSON();
//        Hotspot jsonObj = Hotspot.fromJSON(jObj);
//        Assert.assertEquals(obj, jsonObj);
//        try {
//            jObj.put(JsonKeys.LOGO, "fake-logo-url-new");
//        } catch (JSONException e) {
//        }
//        jsonObj = Hotspot.fromJSON(jObj);
//        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Hotspot");
    }

    public static void testHotspotMap() {
        HotspotMap obj = ModelCreator.getHotspotMap();
        HotspotMap tmp = ModelCreator.getHotspotMap();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, HotspotMap.CREATOR);

        // TODO This contains json array JSON
        SdkTest.logTestWarning(TAG, "HotspotMap", "NO JSON TESTING DONE - Json will fail");
//        JSONArray jObj = obj.toJSON();
//        Dimension d = ObjectCreator.getDimension();
//        HotspotMap jsonObj = HotspotMap.fromJSON(d, jObj);
//        Assert.assertEquals(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "HotspotMap");

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
        Assert.assertNull(Pageflip.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Pageflip jsonObj = Pageflip.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.LOGO, "fake-logo-url-new");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Pageflip.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Pageflip");

    }

    public static void testPermission() {

        Permission obj = ModelCreator.getPermission();
        Permission tmp = ModelCreator.getPermission();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Permission.CREATOR);

        // JSON
        Assert.assertNull(Permission.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Permission jsonObj = Permission.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Permission");

    }

    public static void testTypeahead() {
        Typeahead obj = ModelCreator.getTypeahead();
        Typeahead tmp = ModelCreator.getTypeahead();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Typeahead.CREATOR);

        // JSON
        Assert.assertNull(Typeahead.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Typeahead jsonObj = Typeahead.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.SUBJECT, "fake-subject-new");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Typeahead.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Typeahead");

    }

    public static void testSubscription() {
        Subscription obj = ModelCreator.getSubscription();
        Subscription tmp = ModelCreator.getSubscription();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Subscription.CREATOR);

        // JSON
        Assert.assertNull(Subscription.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Subscription jsonObj = Subscription.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.DEALER_ID, "fake-dealer-new");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Subscription.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Subscription");

    }

    public static void testPricing() {
        Pricing obj = ModelCreator.getPricing();
        Pricing tmp = ModelCreator.getPricing();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Pricing.CREATOR);

        // JSON
        Assert.assertNull(Pricing.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Pricing jsonObj = Pricing.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.PRICE, Double.MAX_VALUE);
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Pricing.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Pricing");

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
        Assert.assertNull(Links.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Links jsonObj = Links.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.WEBSHOP, "not fake-id");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Links.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Links");

    }

    public static void testImages() {
        String id = "fake-id";
        int page = 0;
        Images obj = ModelCreator.getImages(id, page);
        Images tmp = ModelCreator.getImages(id, page);
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Images.CREATOR);

        // JSON
        Assert.assertNull(Images.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Images jsonObj = Images.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.VIEW, "not fake-id");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Images.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Images");

    }

    public static void testSi() {
        Si obj = ModelCreator.getSi();
        Si tmp = ModelCreator.getSi();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Si.CREATOR);

        // JSON
        Assert.assertNull(Si.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Si jsonObj = Si.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.SYMBOL, "not foobar");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Si.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Si");

    }

    public static void testUnit() {
        Unit obj = ModelCreator.getUnit();
        Unit tmp = ModelCreator.getUnit();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Unit.CREATOR);

        // JSON
        Assert.assertNull(Unit.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Unit jsonObj = Unit.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.SYMBOL, "not foobar");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Unit.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Unit");

    }

    public static void testPieces() {
        Pieces obj = ModelCreator.getPieces();
        Pieces tmp = ModelCreator.getPieces();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Pieces.CREATOR);

        // JSON
        Assert.assertNull(Pieces.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Pieces jsonObj = Pieces.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.TO, "4.0");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Pieces.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Pieces");

    }

    public static void testSize() {
        Size obj = ModelCreator.getSize();
        Size tmp = ModelCreator.getSize();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Size.CREATOR);

        // JSON
        Assert.assertNull(Size.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Size jsonObj = Size.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);
        try {
            jObj.put(JsonKeys.TO, "4.0");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Size.fromJSON(jObj);
        Assert.assertNotSame(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Size");

    }

    public static void testQuantity() {
        Quantity obj = ModelCreator.getQuantity();
        Quantity tmp = ModelCreator.getQuantity();
        Assert.assertEquals(obj, tmp);
        Assert.assertEquals(obj.hashCode(), tmp.hashCode());

        // Parcelable
        testParcelable(obj, Quantity.CREATOR);

        // JSON
        Assert.assertNull(Quantity.fromJSON((JSONObject)null));
        JSONObject jObj = obj.toJSON();
        Quantity jsonObj = Quantity.fromJSON(jObj);
        Assert.assertEquals(obj, jsonObj);

        // getters and setters

        SdkTest.logTest(TAG, "Quantity");

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

        // JSON
        Assert.assertNull(Offer.fromJSON((JSONObject)null));
        JSONObject jOffer = obj.toJSON();
        Offer jsonObj = Offer.fromJSON(jOffer);
        Assert.assertEquals(obj, jsonObj);
        try {
            jOffer.put(JsonKeys.HEADING, "Not an offer heading");
        } catch (JSONException e) {
            // ignore
        }
        jsonObj = Offer.fromJSON(jOffer);
        Assert.assertNotSame(obj, jsonObj);

        testIDealer(obj);
        testIStore(obj);
        testICatalog(obj);

        SdkTest.logTest(TAG, "Offer");
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
        if (origId != null) {
            Assert.assertTrue(obj.getErn().startsWith("ern:"));
        } else {
            Assert.assertEquals(null, obj.getErn());
        }

        // The type must always exist
        Assert.assertNotNull(obj.getErnType());
        if (type != null) {
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
