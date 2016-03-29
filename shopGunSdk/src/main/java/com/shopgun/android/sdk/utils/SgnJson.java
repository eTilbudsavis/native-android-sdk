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

package com.shopgun.android.sdk.utils;

import android.graphics.Color;
import android.text.TextUtils;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Branding;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Country;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.model.Dimension;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.model.Links;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.model.Pageflip;
import com.shopgun.android.sdk.model.Permission;
import com.shopgun.android.sdk.model.Pieces;
import com.shopgun.android.sdk.model.Pricing;
import com.shopgun.android.sdk.model.Quantity;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Si;
import com.shopgun.android.sdk.model.Size;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.Unit;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.palette.MaterialColor;
import com.shopgun.android.sdk.palette.SgnColor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Helper class designed to simplify working with JSON in Android - specifically the ShopGun Android SDK.
 * The class holds some static methods for converting data, and ensuring that valid data returns.
 */
public class SgnJson {

    public static final String TAG = Constants.getTag(SgnJson.class);

    /** The date format as returned from the server */
    public static final String API_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";

    /** Single instance of SimpleDateFormat to save time and memory */
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(API_DATE_FORMAT, Locale.US);

    private JSONObject mObject;
    private Stats mStats;

    public SgnJson() {
        this(new JSONObject());
    }

    public SgnJson(JSONObject object) {
        mObject = object == null ? new JSONObject() : object;
        mStats = new Stats();
    }

    public JSONObject toJSON() {
        return mObject;
    }

    public boolean has(String key) {
        return mObject.has(key);
    }

    public static class ErnTypeException extends RuntimeException {

        public ErnTypeException(String ernType, Class<?> clazz) {
            super(String.format("Ern:type \'%s\' cannot be parsed to \'%s\'", ernType, clazz.getSimpleName()));
        }

    }

    public boolean isErnType(String ernType, Class<?> clazz) {
        return hasErnType(ernType);
    }

    public void isErnTypeOrThrow(String ernType, Class<?> clazz) throws ErnTypeException {
        if (!isErnType(ernType, clazz)) {
            throw new ErnTypeException(ernType, clazz);
        }
    }

    /**
     * Convert an API date into a {@link Date}.
     * <p>See {@link #API_DATE_FORMAT}</p>
     *
     * @param date to convert
     * @return a Date object
     */
    public static Date toDate(String date) {
        synchronized (DATE_FORMATTER) {
            try {
                return DATE_FORMATTER.parse(date);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    /**
     * Convert a {@link Date} into a APi date formatted string.
     * <p>See {@link #API_DATE_FORMAT}</p>
     *
     * @param date to convert
     * @return a string
     */
    public static String toString(Date date) {
        synchronized (DATE_FORMATTER) {
            try {
                return DATE_FORMATTER.format(date);
            } catch (NullPointerException e) {
                return null;
            }
        }
    }

    public boolean hasErnType(String type) {
        Ern e = new Ern(getErn());
        return e.getType() != null && e.getType().equals(type);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code null}.
     */
    public Object get(String key) {
        return mObject.opt(key);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public Object get(String key, Object defValue) {
        try {
            mStats.logKey(key);
            return mObject.isNull(key) ? defValue : mObject.get(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code false}.
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public boolean getBoolean(String key, boolean defValue) {
        mStats.logKey(key);
        return mObject.optBoolean(key, defValue);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code 0.0d}.
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0d);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public double getDouble(String key, double defValue) {
        mStats.logKey(key);
        return mObject.optDouble(key, defValue);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code 0.0d}.
     */
    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public float getFloat(String key, float defValue) {
        return (float) mObject.optDouble(key, defValue);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else 0.
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public int getInt(String key, int defValue) {
        try {
            mStats.logKey(key);
            return mObject.isNull(key) ? defValue : mObject.getInt(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code null}.
     */
    public JSONArray getJSONArray(String key) {
        return getJSONArray(key, null);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public JSONArray getJSONArray(String key, JSONArray defValue) {
        try {
            mStats.logKey(key);
            return mObject.isNull(key) ? defValue : mObject.getJSONArray(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code null}.
     */
    public JSONObject getJSONObject(String key) {
        return getJSONObject(key, null);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public JSONObject getJSONObject(String key, JSONObject defValue) {
        try {
            mStats.logKey(key);
            return mObject.isNull(key) ? defValue : mObject.getJSONObject(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code 0}.
     */
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public long getLong(String key, long defValue) {
        mStats.logKey(key);
        return mObject.optLong(key, defValue);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code null}.
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public String getString(String key, String defValue) {
        try {
            mStats.logKey(key);
            // So if it's an actual JSON-null value, then we'll get the string 'null' rather than an actual null. Fuck
            return mObject.isNull(key) ? defValue : mObject.getString(key);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code new Date(0)}.
     */
    public Date getDate(String key) {
        return getDate(key, null);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public Date getDate(String key, Date defValue) {
        Date date = toDate(getString(key));
        return date == null ? defValue : date;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@link Color#BLACK}.
     */
    public int getColor(String key) {
        return getColor(key, Color.BLACK);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public int getColor(String key, int defValue) {
        String rawColor = getString(key);
        if (rawColor != null) {
            try {
                if (rawColor.charAt(0) != '#') {
                    rawColor = '#' + rawColor;
                }
                return Color.parseColor(rawColor);
            } catch (Exception e) {
                // ignore
            }
        }
        return defValue;
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else a {@link MaterialColor}
     * set to {@link Color#BLACK}.
     */
    public MaterialColor getMaterialColor(String key) {
        return getMaterialColor(key, new SgnColor(Color.BLACK));
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public MaterialColor getMaterialColor(String key, MaterialColor defValue) {
        int color = getColor(key, Color.BLACK);
        return (color == Color.BLACK ? defValue : new SgnColor(color));
    }



    public SgnJson put(String key, String value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson put(String key, boolean value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson put(String key, double value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson put(String key, int value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson put(String key, long value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson put(String key, Object value) {
        try {
            mObject.put(key, putCheck(value));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson putColor(String key, int value) {
        try {
            if (Color.alpha(value) != 255) {
                SgnLog.w(TAG, "ShopGun api doesn't support transparency. Transparency will be stripped.");
            }
            String color = String.format("%06X", 0xFFFFFF & value);
            mObject.put(key, putCheck(color));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson putMaterialColor(String key, MaterialColor value) {
        try {
            if (value == null) {
                mObject.put(key, JSONObject.NULL);
            } else {
                putColor(key, value.getValue());
            }
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return this;
    }

    public SgnJson putDate(String key, Date date) {
        put(key, toString(date));
        return this;
    }

    private SgnJson putIJsonIfNotNull(String key, IJson<JSONObject> object) {
        if (object != null) {
            put(key, object.toJSON());
        }
        return this;
    }

    private SgnJson putIJsonIfNotNull(String key, Collection<? extends IJson<JSONObject>> list) {
        if (list != null) {
            JSONArray a = new JSONArray();
            for (IJson<JSONObject> o: list) {
                a.put(o.toJSON());
            }
            put(key, putCheck(a));
        }
        return this;
    }

    private SgnJson putIJson(String key, IJson<JSONObject> object) {
        put(key, putCheck(object));
        return this;
    }

    private SgnJson putIJson(String key, Collection<? extends IJson<JSONObject>> list) {
        if (list == null) {
            put(key, putCheck(null));
        } else {
            putIJsonIfNotNull(key, list);
        }
        return this;
    }

    private static <T> Object putCheck(T object) {
        if (object == null) {
            return JSONObject.NULL;
        } else if (object instanceof IJson<?>) {
            return ((IJson<?>)object).toJSON();
        } else {
            return object;
        }
    }

    public static final String STORE = "store";
    private static final String SDK_STORE = "sdk_store";

    public SgnJson putStore(Store store) {
        putIJsonIfNotNull(STORE, store);
        return this;
    }

    public Store getStore() {
        if (mObject.has(SDK_STORE)) {
            // recover from the legacy key
            return Store.fromJSON(getJSONObject(SDK_STORE));
        }
        return Store.fromJSON(getJSONObject(STORE));
    }

    public static final String CATALOG = "catalog";
    public static final String SDK_CATALOG = "sdk_catalog";

    public SgnJson putCatalog(Catalog catalog) {
        putIJsonIfNotNull(CATALOG, catalog);
        return this;
    }

    public Catalog getCatalog() {
        if (mObject.has(SDK_CATALOG)) {
            // recover from the legacy key
            return Catalog.fromJSON(getJSONObject(SDK_CATALOG));
        }
        return Catalog.fromJSON(getJSONObject(CATALOG));
    }

    public SgnJson putCatalogList(List<Catalog> catalogs) {
        putIJsonIfNotNull(CATALOGS, catalogs);
        return this;
    }

    /**
     * Querying for 'catalogs'
     * @return A list of catalog
     */
    public List<Catalog> getCatalogList() {
        JSONArray a = getJSONArray(CATALOGS);
        return a == null ? null : Catalog.fromJSON(a);
    }

    public static final String OFFERS = "offers";

    public SgnJson putOfferList(List<Offer> offers) {
        putIJsonIfNotNull(OFFERS, offers);
        return this;
    }

    public static final String DEALER = "dealer";
    private static final String SDK_DEALER = "sdk_dealer";

    public SgnJson putDealer(Dealer dealer) {
        putIJsonIfNotNull(DEALER, dealer);
        return this;
    }

    public Dealer getDealer() {
        if (mObject.has(SDK_DEALER)) {
            // recover from the legacy key
            return Dealer.fromJSON(getJSONObject(SDK_DEALER));
        }
        return Dealer.fromJSON(getJSONObject(DEALER));
    }

    public List<Offer> getOfferList() {
        JSONArray a = getJSONArray(OFFERS);
        return a == null ? null : Offer.fromJSON(a);
    }

    public static final String ID = "id";

    public String getId() {
        return getString(ID);
    }

    public SgnJson setId(String value) {
        put(ID, value);
        return this;
    }

    public static final String ERN = "ern";

    public String getErn() {
        return getString(ERN);
    }

    public SgnJson setErn(String value) {
        put(ERN, value);
        return this;
    }

    public static final String NAME = "name";

    public String getName() {
        return getString(NAME);
    }

    public SgnJson setName(String value) {
        put(NAME, value);
        return this;
    }

    public static final String RUN_FROM = "run_from";

    public Date getRunFrom() {
        return getDate(RUN_FROM);
    }

    public SgnJson setRunFrom(Date value) {
        putDate(RUN_FROM, value);
        return this;
    }

    public static final String RUN_TILL = "run_till";

    public Date getRunTill() {
        return getDate(RUN_TILL);
    }

    public SgnJson setRunTill(Date value) {
        putDate(RUN_TILL, value);
        return this;
    }

    public static final String DEALER_ID = "dealer_id";

    public String getDealerId() {
        return getString(DEALER_ID);
    }

    public SgnJson setDealerId(String value) {
        put(DEALER_ID, value);
        return this;
    }

    public static final String DEALER_URL = "dealer_url";

    public String getDealerUrl() {
        return getString(DEALER_URL);
    }

    public SgnJson setDealerUrl(String value) {
        put(DEALER_URL, value);
        return this;
    }

    public static final String STORE_ID = "store_id";

    public String getStoreId() {
        return getString(STORE_ID);
    }

    public SgnJson setStoreId(String value) {
        put(STORE_ID, value);
        return this;
    }

    public static final String STORE_URL = "store_url";

    public String getStoreUrl() {
        return getString(STORE_URL);
    }

    public SgnJson setStoreUrl(String value) {
        put(STORE_URL, value);
        return this;
    }

    public static final String IMAGES = "images";

    public Images getImages() {
        return Images.fromJSON(getJSONObject(IMAGES));
    }

    public SgnJson setImages(Images value) {
        put(IMAGES, putCheck(value));
        return this;
    }

    public static final String BRANDING = "branding";

    public Branding getBranding() {
        return Branding.fromJSON(getJSONObject(BRANDING));
    }

    public SgnJson setBranding(Branding value) {
        put(BRANDING, putCheck(value));
        return this;
    }

    public static final String MODIFIED = "modified";

    public Date getModified() {
        return getDate(MODIFIED, new Date(0));
    }

    public SgnJson setModified(Date value) {
        putDate(MODIFIED, value);
        return this;
    }

    public static final String DESCRIPTION = "description";

    public String getDescription() {
        return getString(DESCRIPTION);
    }

    public SgnJson setDescription(String value) {
        put(DESCRIPTION, value);
        return this;
    }

    public static final String WEBSITE = "website";

    public String getWebsite() {
        return getString(WEBSITE);
    }

    public SgnJson setWebsite(String value) {
        put(WEBSITE, value);
        return this;
    }

    public static final String LOGO = "logo";

    public String getLogo() {
        return getString(LOGO);
    }

    public SgnJson setLogo(String value) {
        put(LOGO, value);
        return this;
    }

    public static final String COLOR = "color";

    public int getColor() {
        return getColor(COLOR);
    }

    public SgnJson setColor(int value) {
        putColor(COLOR, value);
        return this;
    }

    public MaterialColor getMaterialColor() {
        return getMaterialColor(COLOR);
    }

    public SgnJson setMaterialColor(MaterialColor value) {
        putMaterialColor(COLOR, value);
        return this;
    }

    public static final String PAGEFLIP = "pageflip";

    public Pageflip getPageflip() {
        return Pageflip.fromJSON(getJSONObject(PAGEFLIP));
    }

    public SgnJson setPageflip(Pageflip value) {
        put(PAGEFLIP, putCheck(value));
        return this;
    }

    public static final String COUNTRY = "country";

    public Country getCountry() {
        return Country.fromJSON(getJSONObject(COUNTRY));
    }

    public SgnJson setCountry(Country value) {
        put(COUNTRY, putCheck(value));
        return this;
    }

    public static final String ACCESS = "access";

    public String getAccess() {
        return getString(ACCESS);
    }

    public SgnJson setAccess(String value) {
        put(ACCESS, value);
        return this;
    }

    public static final String LABEL = "label";

    public String getLabel() {
        return getString(LABEL);
    }

    public SgnJson setLabel(String value) {
        put(LABEL, value);
        return this;
    }

    public static final String BACKGROUND = "background";

    public int getBackground() {
        return getColor(BACKGROUND);
    }

    public SgnJson setBackground(int value) {
        putColor(BACKGROUND, value);
        return this;
    }

    public SgnJson setBackground(MaterialColor value) {
        putMaterialColor(BACKGROUND, value);
        return this;
    }

    public static final String PAGE_COUNT = "page_count";

    public int getPageCount() {
        return getInt(PAGE_COUNT);
    }

    public SgnJson setPagecount(int value) {
        put(PAGE_COUNT, value);
        return this;
    }

    public static final String FAVORITE_COUNT = "favorite_count";

    public int getFavoriteCount() {
        return getInt(FAVORITE_COUNT);
    }

    public SgnJson setFavoriteCount(int value) {
        put(FAVORITE_COUNT, value);
        return this;
    }

    public static final String OFFER_COUNT = "offer_count";

    public int getOfferCount() {
        return getInt(OFFER_COUNT);
    }

    public SgnJson setOfferCount(int value) {
        put(OFFER_COUNT, value);
        return this;
    }

    public static final String DIMENSIONS = "dimensions";

    public Dimension getDimensions() {
        return Dimension.fromJSON(getJSONObject(DIMENSIONS));
    }

    public SgnJson setDimensions(Dimension value) {
        put(DIMENSIONS, putCheck(value));
        return this;
    }

    public static final String PAGES = "pages";
    private static final String SDK_PAGES = "sdk_pages";

    public SgnJson putPages(List<Images> pages) {
        putIJsonIfNotNull(PAGES, pages);
        return this;
    }

    public List<Images> getPages() {
        if (mObject.has(SDK_PAGES)) {
            // recover from the legacy key
            Images.fromJSON(getJSONArray(SDK_PAGES));
        }
        Object o = mObject.opt(PAGES);
        if (o instanceof JSONObject) {
            JSONObject pages = getJSONObject(PAGES);
            if (pages.has(THUMB) && pages.has(VIEW) && pages.has(ZOOM)) {
                // The API catalog model, contains a JSONObject, with "thumb", "view", "zoom".
                // This is just dummy data, and should be ignored
                return null;
            }
        } else if (o == null || o == JSONObject.NULL) {
            return null;
        }
        return Images.fromJSON(getJSONArray(PAGES));
    }

    public static final String PAGE = "page";
    public static final String OWNER = "owner";
    public static final String TICK = "tick";

    public boolean getTick() {
        return getBoolean(TICK);
    }

    public SgnJson setTick(boolean value) {
        put(TICK, value);
        return this;
    }

    public static final String OFFER_ID = "offer_id";

    public String getOfferId() {
        return getString(OFFER_ID);
    }

    public SgnJson setOfferId(String value) {
        put(OFFER_ID, value);
        return this;
    }

    public static final String COUNT = "count";

    public int getCount() {
        return getInt(COUNT);
    }

    public SgnJson setCount(int value) {
        put(COUNT, value);
        return this;
    }

    public static final String SHOPPINGLIST_ID = "shopping_list_id";

    public String getShoppingListId() {
        return getString(SHOPPINGLIST_ID);
    }

    public SgnJson setShoppingListId(String value) {
        put(SHOPPINGLIST_ID, value);
        return this;
    }

    public static final String CREATOR = "creator";

    public String getCreator() {
        return getString(CREATOR);
    }

    public SgnJson setCreator(String value) {
        put(CREATOR, value);
        return this;
    }

    public static final String HEADING = "heading";

    public String getHeading() {
        return getString(HEADING);
    }

    public SgnJson setHeading(String value) {
        put(HEADING, value);
        return this;
    }

    public static final String CATALOG_PAGE = "catalog_page";

    public int getCatalogPage() {
        return getInt(CATALOG_PAGE);
    }

    public SgnJson setCatalogPage(int value) {
        put(CATALOG_PAGE, value);
        return this;
    }

    public static final String PRICING = "pricing";

    public Pricing getPricing() {
        return Pricing.fromJSON(getJSONObject(PRICING));
    }

    public SgnJson setPricing(Pricing value) {
        put(PRICING, putCheck(value));
        return this;
    }

    public static final String QUANTITY = "quantity";

    public Quantity getQuantity() {
        return Quantity.fromJSON(getJSONObject(QUANTITY));
    }

    public SgnJson setQuantity(Quantity value) {
        put(QUANTITY, putCheck(value));
        return this;
    }

    public static final String LINKS = "links";

    public Links getLinks() {
        return Links.fromJSON(getJSONObject(LINKS));
    }

    public SgnJson setLinks(Links value) {
        put(LINKS, putCheck(value));
        return this;
    }

    public static final String CATALOG_URL = "catalog_url";

    public String getCatalogUrl() {
        return getString(CATALOG_URL);
    }

    public SgnJson setCatalogUrl(String value) {
        put(CATALOG_URL, value);
        return this;
    }

    public static final String CATALOG_ID = "catalog_id";

    public String getCatalogId() {
        return getString(CATALOG_ID);
    }

    public SgnJson setCatalogId(String value) {
        put(CATALOG_ID, value);
        return this;
    }

    public static final String STREET = "street";

    public String getStreet() {
        return getString(STREET);
    }

    public SgnJson setStreet(String value) {
        put(STREET, value);
        return this;
    }

    public static final String CITY = "city";

    public String getCity() {
        return getString(CITY);
    }

    public SgnJson setCity(String value) {
        put(CITY, value);
        return this;
    }

    public static final String ZIP_CODE = "zip_code";

    public String getZipCode() {
        return getString(ZIP_CODE);
    }

    public SgnJson setZipCode(String value) {
        put(ZIP_CODE, value);
        return this;
    }

    public static final String LATITUDE = "latitude";

    public double getLatitude() {
        return getDouble(LATITUDE);
    }

    public SgnJson setLatitude(double value) {
        put(LATITUDE, value);
        return this;
    }

    public static final String LONGITUDE = "longitude";

    public double getLongitude() {
        return getDouble(LONGITUDE);
    }

    public SgnJson setLongitude(double value) {
        put(LONGITUDE, value);
        return this;
    }

    public static final String CONTACT = "contact";

    public String getContact() {
        return getString(CONTACT);
    }

    public SgnJson setContact(String value) {
        put(CONTACT, value);
        return this;
    }

    public static final String WEBSHOP = "webshop";

    public String getWebshop() {
        return getString(WEBSHOP);
    }

    public SgnJson setWebshop(String value) {
        put(WEBSHOP, value);
        return this;
    }

    public static final String WIDTH = "width";

    public double getWidth() {
        return getDouble(WIDTH);
    }

    public SgnJson setWidth(double value) {
        put(WIDTH, value);
        return this;
    }

    public static final String HEIGHT = "height";

    public double getHeight() {
        return getDouble(HEIGHT);
    }

    public SgnJson setHeight(double value) {
        put(HEIGHT, value);
        return this;
    }

    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DETAILS = "details";
    public static final String FAILED_ON_FIELD = "failed_on_field";

    public static final String VIEW = "view";

    public String getView() {
        return getString(VIEW);
    }

    public SgnJson setView(String value) {
        put(VIEW, value);
        return this;
    }

    public static final String ZOOM = "zoom";

    public String getZoom() {
        return getString(ZOOM);
    }

    public SgnJson setZoom(String value) {
        put(ZOOM, value);
        return this;
    }

    public static final String THUMB = "thumb";

    public String getThumb() {
        return getString(THUMB);
    }

    public SgnJson setThumb(String value) {
        put(THUMB, value);
        return this;
    }

    public static final String FROM = "from";

    public double getFrom() {
        return getDouble(FROM, 1.0d);
    }

    public SgnJson setFrom(double value) {
        put(FROM, value);
        return this;
    }

    public static final String TO = "to";

    public double getTo() {
        return getDouble(TO, 1.0d);
    }

    public SgnJson setTo(double value) {
        put(TO, value);
        return this;
    }

    public static final String UNIT = "unit";

    public Unit getUnit() {
        return Unit.fromJSON(getJSONObject(UNIT));
    }

    public SgnJson setUnit(Unit value) {
        put(UNIT, putCheck(value));
        return this;
    }

    public static final String SIZE = "size";

    public Size getSize() {
        return Size.fromJSON(getJSONObject(SIZE));
    }

    public SgnJson setSize(Size value) {
        put(SIZE, putCheck(value));
        return this;
    }

    public static final String PIECES = "pieces";

    public Pieces getPieces() {
        return Pieces.fromJSON(getJSONObject(PIECES));
    }

    public SgnJson setPieces(Pieces value) {
        put(PIECES, putCheck(value));
        return this;
    }

    public static final String USER = "user";

    public User getUser() {
        return User.fromJSON(getJSONObject(USER));
    }

    public SgnJson setUser(User value) {
        // The API doesn't recognize our 'non-user' setup, so we'll null it for now
        put(USER, putCheck( (value == null || value.getUserId() == User.NO_USER) ? null : value));
        return this;
    }

    public static final String ACCEPTED = "accepted";

    public boolean getAccepted() {
        return getBoolean(ACCEPTED);
    }

    public SgnJson setAccepted(boolean value) {
        put(ACCEPTED, value);
        return this;
    }

    public static final String SYMBOL = "symbol";

    public String getSymbol() {
        return getString(SYMBOL);
    }

    public SgnJson setSymbol(String value) {
        put(SYMBOL, value);
        return this;
    }

    public static final String GENDER = "gender";

    public String getGender() {
        return getString(GENDER);
    }

    public SgnJson setGender(String value) {
        put(GENDER, value);
        return this;
    }

    public static final String BIRTH_YEAR = "birth_year";

    public int getBirthYear() {
        return getInt(BIRTH_YEAR);
    }

    public SgnJson setBirthYear(int value) {
        put(BIRTH_YEAR, value);
        return this;
    }

    public static final String EMAIL = "email";

    public String getEmail() {
        return getString(EMAIL);
    }

    public SgnJson setEmail(String value) {
        put(EMAIL, value);
        return this;
    }

    public static final String PERMISSIONS = "permissions";

    public Permission getPermissions() {
        return Permission.fromJSON(getJSONObject(PERMISSIONS));
    }

    public SgnJson setPermissions(Permission value) {
        put(PERMISSIONS, putCheck(value));
        return this;
    }

    public static final String PREVIOUS_ID = "previous_id";

    public String getPreviousId() {
        return getString(PREVIOUS_ID);
    }

    public SgnJson setPreviousId(String value) {
        put(PREVIOUS_ID, value);
        return this;
    }

    public static final String SI = "si";

    public Si getSi() {
        return Si.fromJSON(getJSONObject(SI));
    }

    public SgnJson setSi(Si value) {
        put(SI, putCheck(value));
        return this;
    }

    public static final String FACTOR = "factor";

    public double getFactor() {
        return getDouble(FACTOR, 1.0d);
    }

    public SgnJson setFactor(double value) {
        put(FACTOR, value);
        return this;
    }

    public static final String UNSUBSCRIBE_PRINT_URL = "unsubscribe_print_url";

    public String getUnsubscribePrintUrl() {
        return getString(UNSUBSCRIBE_PRINT_URL);
    }

    public SgnJson setUnsubscribePrintUrl(String value) {
        put(UNSUBSCRIBE_PRINT_URL, value);
        return this;
    }

    public static final String TYPE = "type";

    public String getType() {
        return getString(TYPE);
    }

    public SgnJson setType(String value) {
        put(TYPE, value);
        return this;
    }

    public static final String META = "meta";

    public JSONObject getMeta() {
        return getJSONObject(META, new JSONObject());
    }

    public SgnJson setMeta(JSONObject value) {
        put(META, value);
        return this;
    }

    public static final String SHARES = "shares";

    public Collection<Share> getShares() {
        return Share.fromJSON(getJSONArray(SHARES));
    }

    public SgnJson setShares(Collection<Share> value) {
        putIJson(SHARES, value);
        return this;
    }

    public static final String TOKEN = "token";

    public String getToken() {
        return getString(TOKEN);
    }

    public SgnJson setToken(String value) {
        put(TOKEN, value);
        return this;
    }

    public static final String EXPIRES = "expires";

    public Date getExpires() {
        return getDate(EXPIRES);
    }

    public SgnJson setExpires(Date value) {
        putDate(EXPIRES, value);
        return this;
    }

    public static final String PROVIDER = "provider";

    public String getProvider() {
        return getString(PROVIDER);
    }

    public SgnJson setProvider(String value) {
        put(PROVIDER, value);
        return this;
    }

    public static final String PRICE = "price";

    public double getPrice() {
        return getDouble(PRICE, 1.0d);
    }

    public SgnJson setPrice(double value) {
        put(PRICE, value);
        return this;
    }

    public static final String PREPRICE = "pre_price";

    public Double getPrePrice() {
        try {
            return Double.valueOf(getString(PREPRICE));
        } catch (Exception e) {
            return null;
        }
    }

    public SgnJson setPrePrice(Double value) {
        put(PREPRICE, putCheck(value));
        return this;
    }

    public static final String CURRENCY = "currency";

    public String getCurrency() {
        return getString(CURRENCY);
    }

    public SgnJson setCurrency(String value) {
        put(CURRENCY, value);
        return this;
    }

    public static final String LENGTH = "length";

    public int getLength() {
        return getInt(LENGTH);
    }

    public SgnJson setLength(int value) {
        put(LENGTH, value);
        return this;
    }

    public static final String OFFSET = "offset";

    public int getOffset() {
        return getInt(OFFSET);
    }

    public SgnJson setOffset(int value) {
        put(OFFSET, value);
        return this;
    }

    public static final String SUBJECT = "subject";

    public String getSubject() {
        return getString(SUBJECT);
    }

    public SgnJson setSubject(String value) {
        put(SUBJECT, value);
        return this;
    }

    public static final String ACCEPT_URL = "accept_url";

    public String getAcceptUrl() {
        return getString(ACCEPT_URL);
    }

    public SgnJson setAcceptUrl(String value) {
        put(ACCEPT_URL, value);
        return this;
    }

    public static final String PDF_URL = "pdf_url";

    public String getPdfUrl() {
        return getString(PDF_URL);
    }

    public SgnJson setPdfUrl(String value) {
        put(PDF_URL, value);
        return this;
    }

    public static final String CATEGORY_IDS = "category_ids";

    public Set<String> getCategoryIds() {
        JSONArray json = getJSONArray(CATEGORY_IDS, new JSONArray());
        HashSet<String> cat = new HashSet<String>(json.length());
        for (int i = 0; i < json.length(); i++) {
            try {
                cat.add(json.getString(i));
            } catch (JSONException e) {
                // ignore
            }
        }
        return cat;
    }

    public SgnJson setCategoryIds(Set<String> value) {
        put(CATEGORY_IDS, value == null ? new JSONArray() : new JSONArray(value));
        return this;
    }

    public static final String OFFER = "offer";
    public static final String LOCATIONS = "locations";
    public static final String CLIENT_ID = "client_id";

    public String getClientId() {
        return getString(CLIENT_ID);
    }

    public SgnJson setClientId(String value) {
        put(CLIENT_ID, value);
        return this;
    }

    public static final String REFERENCE = "reference";

    public String getReference() {
        return getString(REFERENCE);
    }

    public SgnJson setReference(String value) {
        put(REFERENCE, value);
        return this;
    }

    public static final String SUBSCRIBED = "subscribed";

    public boolean getSubscribed() {
        return getBoolean(SUBSCRIBED);
    }

    public SgnJson setSubscribed(boolean value) {
        put(SUBSCRIBED, value);
        return this;
    }

    public static final String PAYLOAD = "payload";

    public JSONObject getPayload() {
        return getJSONObject(PAYLOAD);
    }

    public SgnJson setPayload(JSONObject value) {
        put(PAYLOAD, putCheck(value));
        return this;
    }

    public static final String CATALOGS = "catalogs";
    public static final String PAYLOAD_TYPE = "payload_type";

    public String getPayloadType() {
        return getString(PAYLOAD_TYPE);
    }

    public SgnJson setPayloadType(String value) {
        put(PAYLOAD_TYPE, value);
        return this;
    }


    private class Ern {

        private String mType;
        private String mId;

        public Ern(String type, String id) {
            mType = type;
            mId = id;
        }

        public Ern(String ern) {
            if (ern.startsWith("ern")) {
                String[] split = ern.split(":");
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i <= split.length-2; i++) {
                    if (sb.length() > 0) {
                        sb.append(":");
                    }
                    sb.append(split[i]);
                }
                mType = sb.toString();
                mId = split[split.length-1];
            }
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            this.mType = type;
        }

        public String getId() {
            return mId;
        }

        public void setId(String id) {
            this.mId = id;
        }

        @Override
        public String toString() {
            return String.format("ern:%s:%s", mType, mId);
        }

    }

    public Stats getStats() {
        return mStats;
    }

    public class Stats {

        private boolean mValidate = true;
        private String mIdentifier;
        private Set<String> mAccepted;
        private Set<String> mRejected;
        private Set<String> mIgnoreForgotten;
        private Set<String> mIgnoreRejected;

        public Stats() {
            int size = mObject.length();
            // Cannot use SgnJson methods, or we might trigger a rejected key
            mIdentifier = mObject.optString(ERN);
            mAccepted = new HashSet<String>(size);
            mRejected = new HashSet<String>(size);
            mIgnoreForgotten = new HashSet<String>();
            mIgnoreRejected = new HashSet<String>();
        }

        public Set<String> getForgottenKeys() {
            List<String> keys = Utils.copyIterator(mObject.keys());
            Set<String> missing = new HashSet<String>(keys);
            missing.removeAll(mAccepted);
            missing.removeAll(mIgnoreForgotten);
            return missing;
        }

        public Set<String> getRejectedKeys() {
            HashSet<String> rejected = new HashSet<String>(mRejected);
            rejected.removeAll(mIgnoreRejected);
            return rejected;
        }

        private Stats logKey(String key) {
            if (mValidate) {
                if (mObject.has(key)) {
                    mAccepted.add(key);
                } else {
                    mRejected.add(key);
                }
            }
            return this;
        }

        public Stats ignoreForgottenKeys(String... keys) {
            mIgnoreForgotten.addAll(Arrays.asList(keys));
            return this;
        }

        public Stats ignoreRejectedKeys(String... keys) {
            mIgnoreRejected.addAll(Arrays.asList(keys));
            return this;
        }

        public void log(String tag) {
            if (!mValidate) {
                return;
            }
            log(tag, "forgotten", getForgottenKeys());
            log(tag, "rejected", getRejectedKeys());
        }

        private void log(String tag, String type, Set<String> keys) {
            if (!keys.isEmpty()) {
                String text = String.format("%s %s: %s", mIdentifier, type, TextUtils.join(", ", keys));
                SgnLog.d(tag, text.trim());
            }
        }

    }

    /**
     * Method for generating a consistent HashCode for a given JSONArray
     * @param a A JSONArray
     * @return A hashCode
     */
    public static int jsonArrayHashCode(JSONArray a) {
        try {
            return jsonArrayHashCodeInternal(a);
        } catch (JSONException e) {
            e.printStackTrace();
            return a.hashCode();
        }
    }

    private static int jsonArrayHashCodeInternal(JSONArray a) throws JSONException {
        if (a == null) {
            return 0;
        }
        final int prime = 31;
        int result = 1;
        for (int i = 0; i < a.length(); i++) {
            Object o = a.get(i);
            int hash = 0;
            if (o instanceof JSONObject) {
                hash = jsonObjectHashCode((JSONObject) o);
            } else if (o instanceof JSONArray) {
                hash = jsonArrayHashCode((JSONArray) o);
            } else {
                hash = (o == null) ? 0 : o.hashCode();
            }
            result = prime * result + hash;
        }
        return result;
    }

    /**
     * Method for determining equality in two JSONArray's
     * @param one A JSONArray
     * @param two A JSONArray
     * @return true if they are equal, else false
     */
    public static boolean jsonArrayEquals(JSONArray one, JSONArray two) {
        try {
            return jsonEqualsInternal(one, two);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean jsonEqualsInternal(JSONArray one, JSONArray two) throws JSONException {

        if (one == null || two == null) {
            return one == two;
        }
        if (one.length() != two.length()) {
            return false;
        }

        // Set of elements that have been comparedand found equal
        // and therefore cannot be used again
        Set<Integer> used = new HashSet<Integer>(two.length());

        // bubble sort check for equals isn't very efficient
        outerloop:
        for (int i = 0; i < one.length(); i++) {

            Object objOne = one.isNull(i) ? null : one.get(i);

            for (int j = 0; j < two.length(); j++) {

                if (used.contains(i)) {
                    continue;
                }

                Object objTwo = two.isNull(j) ? null : two.get(j);

                if (objOne == null || objTwo == null) {
                    if (objOne == objTwo) {
                        // both null, just continue
                        used.add(j);
                        continue outerloop;
                    } else {
                        return false;
                    }
                }

                if (isEqualJson(objOne, objTwo)) {
                    used.add(j);
                    continue outerloop;
                }

            }
            // we'll only reach this if there wasn't a match/mismatch in the above loop
            return false;

        }
        return true;
    }

    /**
     * Method for generating a consistent HashCode for a given JSONObject
     * @param o A JSONObject
     * @return A hashCode
     */
    public static int jsonObjectHashCode(JSONObject o) {
        try {
            return jsonObjectHashCodeInternal(o);
        } catch (JSONException e) {
            e.printStackTrace();
            return o.hashCode();
        }
    }

    private static int jsonObjectHashCodeInternal(JSONObject o) throws JSONException {
        if (o == null) {
            return 0;
        }
        List<String> keys = new ArrayList<String>();
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();

        final int prime = 31;
        int result = 1;

        for (String key : keys) {
            Object tmp = o.get(key);
            if (tmp instanceof JSONObject) {
                result = prime * result + jsonObjectHashCode((JSONObject) tmp);
            } else if (tmp instanceof JSONArray) {
                result = prime * result + jsonArrayHashCode((JSONArray) tmp);
            } else {
                sb.append(key).append(tmp);
            }
        }
        result = prime * result + sb.toString().hashCode();
        return result;
    }

    /**
     * Method for determining equality in two JSONObject's
     * @param one A JSONObject
     * @param two A JSONObject
     * @return true if they are equal, else false
     */
    public static boolean jsonObjectEquals(JSONObject one, JSONObject two) {
        try {
            return jsonEqualsInternal(one, two);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean jsonEqualsInternal(JSONObject one, JSONObject two) throws JSONException {
        if (one == null || two == null) {
            return one == two;
        }
        if (one.length() != two.length()) {
            return false;
        }

        Iterator<String> it = one.keys();
        while (it.hasNext()) {
            String key = it.next();
            if (!two.has(key)) {
                return false;
            }

            Object objOne = one.isNull(key) ? null : one.get(key);
            Object objTwo = two.isNull(key) ? null : two.get(key);

            if (objOne == null || objTwo == null) {
                if (objOne == objTwo) {
                    // both null, just continue
                    continue;
                } else {
                    return false;
                }
            }

            if (!isEqualJson(objOne, objTwo)) {
                return false;
            }

        }

        return true;
    }

    private static boolean isEqualJson(Object one, Object two) {

        if (one instanceof JSONObject) {
            return (two instanceof JSONObject) && jsonObjectEquals((JSONObject) one, (JSONObject) two);
        }

        if (one instanceof JSONArray) {
            return (two instanceof JSONArray) && jsonArrayEquals((JSONArray) one, (JSONArray) two);
        }

        return one.equals(two);

    }

}
