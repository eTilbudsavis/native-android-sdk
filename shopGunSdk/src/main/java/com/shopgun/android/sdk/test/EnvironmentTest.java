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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.api.Environment;

import junit.framework.TestCase;

public class EnvironmentTest extends TestCase {

    public static final String TAG = Constants.getTag(EnvironmentTest.class);

    private static final String PROD = "https://api.etilbudsavis.dk";
    private static final String EDGE = "https://api-edge.etilbudsavis.dk";
    private static final String STAG = "https://api-staging.etilbudsavis.dk";

    public static void test() {

        SdkTest.start(TAG);
        testEnvironment();
        testApply();
        testFromString();

    }

    public static void testEnvironment() {

        assertEquals(Environment.PRODUCTION.toString(), PROD);
        assertEquals(Environment.EDGE.toString(), EDGE);
        assertEquals(Environment.STAGING.toString(), STAG);
        // CUSTOM, can be anything, but null
        assertNotNull(Environment.CUSTOM.toString());

        SdkTest.logTest(TAG, "Environment");

    }

    public static void testApply() {

        Environment e = Environment.PRODUCTION;

        // null
        String nul = e.apply(null);
        assertEquals(PROD, nul);

        // empty
        String empty = e.apply("");
        assertEquals(PROD, empty);

        String etaApiUrl = PROD + "/some/path";

        // Standard case
        String path = "/some/path";
        String standard = e.apply(path);
        assertEquals(etaApiUrl, standard);

        // non prefixed path
        String noSlashPath = "some/path";
        String noSlash = e.apply(noSlashPath);
        assertEquals(etaApiUrl, noSlash);

        // full ShopGun url
        String fullBuild = e.apply(etaApiUrl);
        assertEquals(etaApiUrl, fullBuild);

        // Any http prefixed url
        String httpUrl = "http://www.jubii.dk/";
        String http = e.apply(httpUrl);
        assertEquals(httpUrl, http);

        // Any https prefixed url
        String httpsUrl = "https://www.jubii.dk/";
        String https = e.apply(httpsUrl);
        assertEquals(httpsUrl, https);

        SdkTest.logTest(TAG, "Apply");

    }

    public static void testFromString() {

        String customOriginal = Environment.CUSTOM.toString();

        // enums must match them selves
        String prod1 = Environment.PRODUCTION.toString();
        assertEquals(Environment.PRODUCTION, Environment.fromString(prod1));

        String edge1 = Environment.EDGE.toString();
        assertEquals(Environment.EDGE, Environment.fromString(edge1));

        String staging1 = Environment.STAGING.toString();
        assertEquals(Environment.STAGING, Environment.fromString(staging1));

        // Some sane defaults on the custom environment
        assertEquals(Environment.CUSTOM, Environment.fromString(null));
        assertEquals(Environment.PRODUCTION.toString(), Environment.CUSTOM.toString());
        assertEquals(Environment.CUSTOM, Environment.fromString(""));
        assertEquals(Environment.PRODUCTION.toString(), Environment.CUSTOM.toString());

        // But allow pretty much any thing
        assertEquals(Environment.CUSTOM, Environment.fromString("random"));
        assertEquals(Environment.CUSTOM, Environment.fromString("https://api.shopgun.c"));
        assertEquals(Environment.CUSTOM, Environment.fromString("https://api.shopgun.com/"));
        assertEquals(Environment.CUSTOM, Environment.fromString("https://shopgun.com/"));

        // Only one custon environment allowed
        String first_url = "first_url";
        String second_url = "second_url";

        Environment custom = Environment.fromString(first_url);

        // Now custom is a instance of Environment.CUSTOM, and has the string value of "first_url" (both custom, and CUSTOM)
        assertTrue(custom == Environment.CUSTOM);
        assertEquals(first_url, custom.toString());
        assertEquals(first_url, Environment.CUSTOM.toString());

        // Lets change Environment.CUSTOM
        Environment.fromString(second_url);

        // custom (instance of CUSTOM) now has a different value - as only one CUSTOM environment is allowed
        assertNotSame(first_url, custom.toString());

        // But both CUSTOM, and custom matches "second_url"
        assertEquals(second_url, Environment.CUSTOM.toString());
        assertEquals(second_url, custom.toString());

        // Any string matching an environment must return the environment
        assertEquals(Environment.PRODUCTION, Environment.fromString(PROD));
        assertEquals(Environment.EDGE, Environment.fromString(EDGE));
        assertEquals(Environment.STAGING, Environment.fromString(STAG));

        assertNotSame(Environment.PRODUCTION, Environment.EDGE);
        assertNotSame(Environment.PRODUCTION, Environment.STAGING);
        assertNotSame(Environment.PRODUCTION, Environment.CUSTOM);
        assertNotSame(Environment.EDGE, Environment.STAGING);
        assertNotSame(Environment.EDGE, Environment.CUSTOM);
        assertNotSame(Environment.STAGING, Environment.CUSTOM);

        // Set CUSTOM to it's original value
        Environment.setCustom(customOriginal);

        SdkTest.logTest(TAG, "FromString");

    }

}
