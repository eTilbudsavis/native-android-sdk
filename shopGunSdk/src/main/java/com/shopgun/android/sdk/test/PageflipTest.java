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
import com.shopgun.android.sdk.pageflip.stats.EventType;
import com.shopgun.android.sdk.pageflip.stats.Orientation;
import com.shopgun.android.sdk.pageflip.stats.PageEvent;
import com.shopgun.android.sdk.pageflip.stats.PageflipStatsCollector;
import com.shopgun.android.sdk.pageflip.stats.impl.PageflipStatsCollectorImpl;
import com.shopgun.android.sdk.test.impl.TestClock;
import com.shopgun.android.sdk.test.impl.TestStatDelivery;

import junit.framework.TestCase;

public class PageflipTest extends TestCase {

    public static final String TAG = Constants.getTag(PageflipTest.class);

    public static void test() {

        SdkTest.start(TAG);
        testPageEvent();
        testPageStatCollector();

    }

    public static void testPageEvent() {

        TestClock clock = new TestClock();
        String viewSession = "my-session";
        int[] pages = new int[]{1};

        PageEvent e = new PageEvent(EventType.VIEW, viewSession, Orientation.LANDSCAPE, pages, clock);

        assertFalse(e.isActive());
        assertFalse(e.isCollected());
        assertFalse(e.isStarted());
        assertFalse(e.isStopped());

        assertEquals(0, e.getDuration());
        assertEquals(0, e.getDurationAbsolute());

        assertEquals(0, e.getSubEvents().size());
        assertEquals(0, e.getSubEventsRecursive().size());

        assertEquals(0, e.getStart());
        assertEquals(0, e.getStop());

        e.start();
        assertEquals(0, e.getStart());

        clock.increment(); // time == 10
        assertEquals(10, e.getDuration());
        assertEquals(10, e.getDurationAbsolute());


        assertTrue(e.isActive());
        assertFalse(e.isCollected());
        assertTrue(e.isStarted());
        assertFalse(e.isStopped());

        clock.increment(); // time == 20
        e.stop();
        assertEquals(20, e.getDurationAbsolute());
        assertEquals(20, e.getDuration());

        assertFalse(e.isActive());
        assertFalse(e.isCollected());
        assertTrue(e.isStarted());
        assertTrue(e.isStopped());

        e.reset();
        assertFalse(e.isActive());
        assertFalse(e.isCollected());
        assertFalse(e.isStarted());
        assertFalse(e.isStopped());

        clock.reset();
        e.start();
        clock.increment(); // time == 10
        for (int i = 0; i < 10; i++) {
            PageEvent subEvent = PageEvent.zoom(viewSession, pages, clock);
            subEvent.start();
            e.addSubEvent(subEvent);
            clock.increment();
            subEvent.stop();
        }

        assertEquals(10, e.getDuration());
        assertEquals(110, e.getDurationAbsolute());
        e.stop();

        SdkTest.logTest(TAG, "PageEvent");
    }

    public static void testPageStatCollector() {

        String viewSession =  "my-view-session";
        String catalogId = "my-catalog-id";
        int[] pages = new int[]{1,2};
        TestClock clock = new TestClock();
        TestStatDelivery delivery = new TestStatDelivery();

        PageflipStatsCollector collector = new PageflipStatsCollectorImpl(viewSession, catalogId, pages, clock, delivery);

        testPageflipStatsCollector(collector, clock, delivery);

    }

    public static void testPageflipStatsCollector(PageflipStatsCollector collector, TestClock clock, TestStatDelivery delivery) {

        reset(collector, clock, delivery);

        assertEquals(0, collector.getEvents().size());
        collector.startView();
        assertEquals(1, collector.getEvents().size());

        clock.increment();  // time == 20
        collector.startZoom();
        assertEquals(2, collector.getEvents().size());

        clock.increment(); // time == 30
        collector.stopZoom();
        assertEquals(2, collector.getEvents().size());

        clock.increment(); // time == 40
        collector.stopView();
        assertEquals(2, collector.getEvents().size());

        assertEquals(20, collector.getRootEvent().getDuration());
        assertEquals(30, collector.getRootEvent().getDurationAbsolute());

        reset(collector, clock, delivery);

        // Try invoking only the zoom event
        collector.startZoom();
        assertEquals(2, collector.getEvents().size());
        clock.increment(); // time == 20
        delivery.reset();
        collector.collect();
        assertTrue(delivery.recievedCollect());
        assertTrue(collector.getRootEvent().isCollected());
        assertEquals(0, collector.getRootEvent().getDuration());
        assertEquals(10, collector.getRootEvent().getDurationAbsolute());


        // There is nothing to collect
        reset(collector, clock, delivery);
        collector.collect();
        assertFalse(delivery.recievedCollect());

        reset(collector, clock, delivery);
        collector.stopView();
        collector.collect();
        assertTrue(delivery.recievedCollect());
        assertEquals(0, collector.getRootEvent().getDurationAbsolute());

        reset(collector, clock, delivery);
        collector.stopZoom();
        clock.increment();
        collector.collect();
        assertTrue(delivery.recievedCollect());
        assertEquals(2, collector.getEvents().size());
        assertEquals(10, collector.getRootEvent().getDurationAbsolute());
        assertEquals(0, collector.getRootEvent().getSubEvents().get(0).getDurationAbsolute());

        reset(collector, clock, delivery);

    }

    private static void reset(PageflipStatsCollector collector, TestClock clock, TestStatDelivery delivery) {
        collector.reset();
        clock.reset(); // time == 10
        delivery.reset();
        assertEquals(0, collector.getEvents().size());
    }

}
