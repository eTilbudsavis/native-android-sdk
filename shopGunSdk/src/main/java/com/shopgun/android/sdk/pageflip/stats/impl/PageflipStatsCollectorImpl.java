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

package com.shopgun.android.sdk.pageflip.stats.impl;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pageflip.Orientation;
import com.shopgun.android.sdk.pageflip.ReaderConfig;
import com.shopgun.android.sdk.pageflip.stats.Clock;
import com.shopgun.android.sdk.pageflip.stats.PageEvent;
import com.shopgun.android.sdk.pageflip.stats.PageflipStatsCollector;
import com.shopgun.android.sdk.pageflip.stats.StatDelivery;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import java.util.ArrayList;
import java.util.List;

public class PageflipStatsCollectorImpl implements PageflipStatsCollector {

    public static final String TAG = Constants.getTag(PageflipStatsCollectorImpl.class);

    private static final boolean LOG = false;

    private final String mViewSession;
    private final String mCatalogId;
    private final int[] mPages;
    private final Orientation mOrientation;
    private final Clock mClock;
    private final StatDelivery mDelivery;
    private PageEvent mView;
    private PageEvent mZoom;

    public PageflipStatsCollectorImpl(String viewSession, Catalog catalog, int[] pages, ReaderConfig config, Clock clock, StatDelivery delivery) {
        this(viewSession, catalog.getId(), pages, config.getOrientation(), clock, delivery);
    }

    public PageflipStatsCollectorImpl(String viewSession, String catalogId, int[] pages, Orientation orientation, Clock clock, StatDelivery delivery) {
        mViewSession = viewSession;
        mCatalogId = catalogId;
        mPages = pages;
        mOrientation = orientation;
        mClock = clock;
        mDelivery = delivery;
    }

    @Override
    public void startView() {
        log("startView");
        if (mView == null) {
            mView = PageEvent.view(mViewSession, mPages, mOrientation, mClock);
        }
        mView.start();
    }

    @Override
    public void stopView() {
        log("stopView");
        if (mView == null) {
            mView = PageEvent.view(mViewSession, mPages, mOrientation, mClock);
        }
        if (mZoom != null) {
            stopZoom();
        }
        mView.stop();
    }

    @Override
    public void startZoom() {
        log("startZoom");
        startView();
        if (mZoom != null) {
            stopZoom();
        }
        mZoom = PageEvent.zoom(mViewSession, mPages, mOrientation, mClock);
        mView.addSubEvent(mZoom);
        mZoom.start();
    }

    @Override
    public void stopZoom() {
        log("stopZoom");
        if (mZoom == null) {
            startView();
            mZoom = PageEvent.zoom(mViewSession, mPages, mOrientation, mClock);
            mView.addSubEvent(mZoom);
        }
        mZoom.stop();
        mZoom = null;
    }

    @Override
    public void collect() {
        log("collect");
        if (mView == null) {
            log("Nothing to collect, ignoring");
            return;
        } else if (mView.isCollected()) {
            log("Already collected, ignore");
            return;
        }
        stopView();
        for (PageEvent ve : getEvents()) {
            if (ve.isCollected()) {
                return;
            }
            ve.setCollected(true);
            mDelivery.deliver(Endpoints.catalogCollect(mCatalogId), ve.toJSON());
        }
    }

    public PageEvent getCurrentEvent() {
        return mView;
    }

    @Override
    public PageEvent getRootEvent() {
        return mView;
    }

    @Override
    public List<PageEvent> getEvents() {
        ArrayList<PageEvent> list = new ArrayList<PageEvent>();
        if (mView != null) {
            list.add(mView);
            list.addAll(mView.getSubEventsRecursive());
        }
        return list;
    }

    @Override
    public void reset() {
        mView = null;
        mZoom = null;
    }

    private void log(String s) {
        if (LOG) {
            SgnLog.d(TAG, "page[" + PageflipUtils.join("-", mPages) + "] " + s);
        }
    }

    @Override
    public String toString() {
        String format = "%s[ viewSession:%s, catalogId:%s, pages:%s, orientation:%s, clock:%s, delivery:%s ]";
        return String.format(format, getClass().getSimpleName(), mViewSession, mCatalogId, PageflipUtils.join("-", mPages), mOrientation.toString(), mClock.getClass().getSimpleName(), mDelivery.getClass().getSimpleName());
    }
}
