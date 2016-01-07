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

package com.shopgun.android.sdk.pageflip.stats;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.log.AppLogEntry;
import com.shopgun.android.sdk.log.Event;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PageStatsCollectorImpl implements PageStatsCollector {

    public static final String TAG = Constants.getTag(PageStatsCollectorImpl.class);

    private static final boolean LOG = false;

    private ShopGun mShopgun;
    private String mViewSession;
    private String mCatalogId;
    private int[] mPages;
    private Clock mClock;
    private PageEvent mView;
    private PageEvent mZoom;

    public PageStatsCollectorImpl(ShopGun sgn, String viewSession, String catalogId, int[] pages, Clock clock) {
        mShopgun = sgn;
        mViewSession = viewSession;
        mCatalogId = catalogId;
        mPages = pages;
        mClock = clock;
    }

    @Override
    public void startView() {
        log("startView");
        if (mView == null) {
            mView = PageEvent.view(mViewSession, mPages, mClock);
        }
        mView.start();
    }

    @Override
    public void stopView() {
        log("stopView");
        if (mView == null) {
            mView = PageEvent.view(mViewSession, mPages, mClock);
        }
        stopZoom();
        mView.stop();
    }

    @Override
    public void startZoom() {
        log("startZoom");
        stopZoom();
        mZoom = PageEvent.zoom(mViewSession, mPages, mClock);
        mView.addSubEvent(mZoom);
        mZoom.start();
    }

    @Override
    public void stopZoom() {
        log("stopZoom");
        if (mZoom != null) {
            mZoom.stop();
            mZoom = null;
        }
    }

    @Override
    public void collect() {
        log("collect");
        if (mView == null) {
            log("Nothing to collect, ignoring");
            return;
        }
        if (mView.isCollected()) {
            log("Already collected, ignore");
            return;
        }
        stopView();
        for (PageEvent ve : getEvents()) {
            postEvent(ve);
        }
        verifyIntegrity();
    }

    private void postEvent(PageEvent ve) {
        if (ve.isCollected()) {
            return;
        }
        ve.setCollected(true);

        String url = Endpoints.catalogCollect(mCatalogId);
        final JSONObject body = ve.toJSON();
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                logOut("Collected: " + body.toString() + ", " + (error==null? response.toString() : error.toString()));
            }
        });
        mShopgun.add(r);
    }

    private void verifyIntegrity() {

        if (mView.getDurationAbsolute() < 0) {

            ShopGun sgn = ShopGun.getInstance();
            AppLogEntry entry = new AppLogEntry(sgn, "negative-duration", "android@shopgun.com");
            for (PageEvent e : getEvents()) {
                entry.addEvent(new Event(sgn, "view-event").setData(e.toJSON()));
            }
            entry.post();

        }

    }

    public PageEvent getCurrentEvent() {
        return mView;
    }

    @Override
    public List<PageEvent> getEvents() {
        ArrayList<PageEvent> list = new ArrayList<PageEvent>();
        list.add(mView);
        list.addAll(mView.getSubEventsRecursive());
        return list;
    }

    private void log(String s) {
        if (LOG) {
            logOut(s);
        }
    }

    private void logOut(String s) {
        SgnLog.d(TAG, "page[" + PageflipUtils.join("-", mPages) + "] " + s);
    }

}
