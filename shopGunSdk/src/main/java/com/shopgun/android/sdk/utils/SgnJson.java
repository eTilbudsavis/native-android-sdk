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
import com.shopgun.android.sdk.model.Pricing;
import com.shopgun.android.sdk.model.Quantity;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.palette.MaterialColor;
import com.shopgun.android.sdk.palette.SgnColor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
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

    JSONObject mObject;

    public SgnJson() {
        mObject = new JSONObject();
        init();
    }

    public SgnJson(JSONObject object) {
        mObject = object;
        init();
    }

    private void init() {
        if (mTestKeys) {
            mGoodKeys = new HashSet<String>(mObject.length());
            mBadKeys = new HashSet<String>(mObject.length());
        }
    }

    public JSONObject toJSON() {
        return mObject;
    }

    public boolean has(String key) {
        return mObject.has(key);
    }

    private Set<String> mGoodKeys;
    private Set<String> mBadKeys;
    private boolean mTestKeys = true;

    private void testKey(String key) {
        if (!mTestKeys || mObject == null) {
            return;
        }

        if (mObject.has(key)) {
            mGoodKeys.add(key);
        } else {
            mBadKeys.add(key);
        }
    }

    public Set<String> getForgottenKeys() {
        Set<String> missing = new HashSet<String>();
        if (!mTestKeys || mObject == null) {
            return missing;
        }
        List<String> keys = Utils.copyIterator(mObject.keys());
        for (String key : keys) {
            if (!mGoodKeys.contains(key)) {
                missing.add(key);
            }
        }
        return missing;
    }

    public Set<String> getSwingAndAMiss() {
        if (!mTestKeys || mObject == null) {
            return new HashSet<String>();
        } else {
            return new HashSet<String>(mBadKeys);
        }
    }

    public void printParsingStatus(String tag) {
        if (!mTestKeys) {
            return;
        }
        Set<String> forgottenKeys = getForgottenKeys();
        if (!forgottenKeys.isEmpty()) {
            SgnLog.d(tag, "ForgottenKeys: " + TextUtils.join(", ", forgottenKeys) );
        }
        Set<String> swingAndAMiss = getSwingAndAMiss();
        if (!swingAndAMiss.isEmpty()) {
            SgnLog.d(tag, "swingAndAMiss: " + TextUtils.join(",", swingAndAMiss));
        }

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
            testKey(key);
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
        testKey(key);
        return mObject.optBoolean(key, defValue);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@link Double#NaN}.
     */
    public double getDouble(String key) {
        return getDouble(key, Double.NaN);
    }

    /**
     * Returns the value mapped by {@code key} if it exists, coercing it if necessary else {@code defValue}.
     */
    public double getDouble(String key, double defValue) {
        testKey(key);
        return mObject.optDouble(key, defValue);
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
            testKey(key);
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
            testKey(key);
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
            testKey(key);
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
        testKey(key);
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
        testKey(key);
        return mObject.optString(key, defValue);
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

    private SgnJson putIfNotNull(String key, IJson<JSONObject> object) {
        if (object != null) {
            put(key, object.toJSON());
        }
        return this;
    }

    private SgnJson putIfNotNull(String key, List<? extends IJson<JSONObject>> list) {
        if (list != null) {
            JSONArray a = new JSONArray();
            for (IJson<JSONObject> o: list) {
                a.put(o.toJSON());
            }
            put(key, putCheck(a));
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

    public SgnJson putStore(Store store) {
        putIfNotNull(STORE, store);
        return this;
    }

    public Store getStore() {
        return Store.fromJSON(getJSONObject(STORE));
    }

    public static final String CATALOG = "catalog";

    public SgnJson putCatalog(Catalog catalog) {
        putIfNotNull(CATALOG, catalog);
        return this;
    }

    public Catalog getCatalog() {
        return Catalog.fromJSON(getJSONObject(CATALOG));
    }

    public SgnJson putCatalogList(List<Catalog> catalogs) {
        putIfNotNull(CATALOGS, catalogs);
        return this;
    }

    public List<Catalog> getCatalogList() {
        JSONArray a = getJSONArray(CATALOGS);
        return a == null ? null : Catalog.fromJSON(a);
    }

    public static final String OFFERS = "offers";

    public SgnJson putOfferList(List<Offer> offers) {
        putIfNotNull(OFFERS, offers);
        return this;
    }

    public static final String DEALER = "dealer";

    public SgnJson putDealer(Dealer dealer) {
        putIfNotNull(DEALER, dealer);
        return this;
    }

    public Dealer getDealer() {
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
        return getDate(MODIFIED);
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

    public SgnJson getWebsite(String value) {
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

    public static final String PAGEFLIP = "pageflip";

    public JSONObject getPageflip() {
        return getJSONObject(PAGEFLIP);
    }

    public SgnJson getPageflip(JSONObject value) {
        put(PAGEFLIP, value);
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

    public SgnJson putPages(List<Images> pages) {
        putIfNotNull(PAGES, pages);
        return this;
    }

    public List<Images> getPages() {
        if (mObject.opt(PAGES) instanceof JSONObject) {
            JSONObject pages = getJSONObject(PAGES);
            if (pages.has(THUMB) && pages.has(VIEW) && pages.has(ZOOM)) {
                // The API catalog model, contains a JSONObject, with "thumb", "view", "zoom".
                // This is just dummy data, and should be ignored
                return null;
            }
        }
        return Images.fromJSON(getJSONArray(PAGES, new JSONArray()));
    }

    public static final String PAGE = "page";
    public static final String OWNER = "owner";
    public static final String TICK = "tick";
    public static final String OFFER_ID = "offer_id";
    public static final String COUNT = "count";
    public static final String SHOPPINGLIST_ID = "shopping_list_id";
    public static final String CREATOR = "creator";

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
    public static final String CITY = "city";
    public static final String ZIP_CODE = "zip_code";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String CONTACT = "contact";
    public static final String WEBSHOP = "webshop";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DETAILS = "details";
    public static final String FAILED_ON_FIELD = "failed_on_field";
    public static final String VIEW = "view";
    public static final String ZOOM = "zoom";
    public static final String THUMB = "thumb";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String UNIT = "unit";
    public static final String SIZE = "size";
    public static final String PIECES = "pieces";
    public static final String USER = "user";
    public static final String ACCEPTED = "accepted";
    public static final String SYMBOL = "symbol";
    public static final String GENDER = "gender";
    public static final String BIRTH_YEAR = "birth_year";
    public static final String EMAIL = "email";
    public static final String PERMISSIONS = "permissions";
    public static final String PREVIOUS_ID = "previous_id";
    public static final String SI = "si";
    public static final String FACTOR = "factor";
    public static final String UNSUBSCRIBE_PRINT_URL = "unsubscribe_print_url";
    public static final String TYPE = "type";
    public static final String META = "meta";
    public static final String SHARES = "shares";
    public static final String TOKEN = "token";
    public static final String EXPIRES = "expires";

    public Date getExpires() {
        return getDate(EXPIRES);
    }

    public SgnJson setExpires(Date expires) {
        putDate(EXPIRES, expires);
        return this;
    }

    public static final String PROVIDER = "provider";
    public static final String PRICE = "price";
    public static final String PREPRICE = "pre_price";
    public static final String CURRENCY = "currency";
    public static final String LENGTH = "length";
    public static final String OFFSET = "offset";
    public static final String SUBJECT = "subject";
    public static final String ACCEPT_URL = "accept_url";
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
        put(CATEGORY_IDS, new JSONArray(value));
        return this;
    }

    public static final String OFFER = "offer";
    public static final String LOCATIONS = "locations";
    public static final String CLIENT_ID = "client_id";
    public static final String REFERENCE = "reference";
    public static final String SUBSCRIBED = "subscribed";
    public static final String PAYLOAD = "payload";

    public static final String CATALOGS = "catalogs";
    public static final String PAYLOAD_TYPE = "payload_type";

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

}
