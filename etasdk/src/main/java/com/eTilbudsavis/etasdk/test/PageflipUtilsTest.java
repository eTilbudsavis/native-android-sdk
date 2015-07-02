package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.HotspotMap;
import com.eTilbudsavis.etasdk.model.Images;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;

public class PageflipUtilsTest {

    public static final String TAG = Constants.getTag(PageflipUtilsTest.class);

    private static final boolean LANDSCAPE = true;
    private static final boolean PORTRAIT = false;

    private PageflipUtilsTest() {
        // empty
    }

    public static void test() {

        EtaSdkTest.start(TAG);
        testPageToPosition();
        testPositionToPage();
        testJoin();
        testCatalogUtils();

    }

    public static void testPageToPosition() {

        testPageToPosition(1, LANDSCAPE, 0);
        testPageToPosition(2, LANDSCAPE, 1);
        testPageToPosition(3, LANDSCAPE, 1);
        testPageToPosition(4, LANDSCAPE, 2);
        testPageToPosition(5, LANDSCAPE, 2);

        testPageToPosition(1, PORTRAIT, 0);
        testPageToPosition(2, PORTRAIT, 1);
        testPageToPosition(3, PORTRAIT, 2);
        testPageToPosition(4, PORTRAIT, 3);

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPageToPosition(int page, boolean land, int expectedPos) {
        Assert.assertEquals(expectedPos, PageflipUtils.pageToPosition(page, land));
    }

    public static void testPositionToPage() {

        int PAGE_COUNT = 8;
        testPositionToPage(0, PAGE_COUNT, LANDSCAPE, new int[]{1});
        testPositionToPage(1, PAGE_COUNT, LANDSCAPE, new int[]{2, 3});
        testPositionToPage(2, PAGE_COUNT, LANDSCAPE, new int[]{4, 5});
        testPositionToPage(3, PAGE_COUNT, LANDSCAPE, new int[]{6, 7});
        testPositionToPage(4, PAGE_COUNT, LANDSCAPE, new int[]{8});

        PAGE_COUNT = 4;
        testPositionToPage(0, PAGE_COUNT, PORTRAIT, new int[]{1});
        testPositionToPage(1, PAGE_COUNT, PORTRAIT, new int[]{2});
        testPositionToPage(2, PAGE_COUNT, PORTRAIT, new int[]{3});
        testPositionToPage(3, PAGE_COUNT, PORTRAIT, new int[]{4});
        testPositionToPage(4, PAGE_COUNT, PORTRAIT, new int[]{5});

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPositionToPage(int pos, int pageCount, boolean land, int[] expectedPages) {
        int[] pages = PageflipUtils.positionToPages(pos, pageCount, land);
        Assert.assertTrue(Arrays.equals(pages, expectedPages));
    }

    public static void testJoin() {

        Assert.assertEquals("0,1,2", PageflipUtils.join(",", new int[]{0, 1, 2}));
        Assert.assertEquals("100,200,300", PageflipUtils.join(",", new int[]{100, 200, 300}));

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
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
        pages.add(ModelCreator.getImages(c.getId()));
        pages.add(ModelCreator.getImages(c.getId()));
        pages.add(ModelCreator.getImages(c.getId()));
        c.setHotspots(map);
        Assert.assertTrue(PageflipUtils.isHotspotsReady(c));
        Assert.assertTrue(PageflipUtils.isPagesReady(c));
        Assert.assertTrue(PageflipUtils.isCatalogReady(c));

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

}
