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

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import junit.framework.Assert;

import java.util.List;

public class PageflipUtilsTest {

    public static final String TAG = Constants.getTag(PageflipUtilsTest.class);

    private static final boolean LANDSCAPE = true;
    private static final boolean PORTRAIT = false;

    private PageflipUtilsTest() {
        // empty
    }

    public static void test() {

        SdkTest.start(TAG);
        testJoin();
        testCatalogUtils();

    }

    public static void testJoin() {

        Assert.assertEquals("0,1,2", PageflipUtils.join(",", new int[]{0, 1, 2}));
        Assert.assertEquals("100,200,300", PageflipUtils.join(",", new int[]{100, 200, 300}));

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testCatalogUtils() {

        Catalog c = ModelCreator.getCatalog();
        Assert.assertTrue(PageflipUtils.isHotspotsReady(c));
        Assert.assertTrue(PageflipUtils.isPagesReady(c));
        Assert.assertTrue(PageflipUtils.isCatalogReady(c));

        // remove the hotspots
        HotspotMap map = c.getHotspots();
        c.setHotspots(null);
        Assert.assertFalse(PageflipUtils.isHotspotsReady(c));
        Assert.assertFalse(PageflipUtils.isCatalogReady(c));

        // remove pages
        List<Images> pages = c.getPages();
        c.setPages(null);
        Assert.assertFalse(PageflipUtils.isPagesReady(c));
        Assert.assertFalse(PageflipUtils.isCatalogReady(c));

        // Empty array of pages
        pages.clear();
        c.setPages(pages);
        Assert.assertFalse(PageflipUtils.isPagesReady(c));
        Assert.assertFalse(PageflipUtils.isCatalogReady(c));

        // refill pages and add map
        pages.add(ModelCreator.getImages(c.getId(), 0));
        pages.add(ModelCreator.getImages(c.getId(), 1));
        pages.add(ModelCreator.getImages(c.getId(), 2));
        c.setHotspots(map);
        Assert.assertTrue(PageflipUtils.isHotspotsReady(c));
        Assert.assertTrue(PageflipUtils.isPagesReady(c));
        Assert.assertTrue(PageflipUtils.isCatalogReady(c));

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

}
