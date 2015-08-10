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

package com.shopgun.android.sdk.pageflip;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request.Method;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.shopgun.android.sdk.utils.Api;

import org.json.JSONException;
import org.json.JSONObject;

public class PageStat {

    public static final String TAG = Constants.getTag(PageStat.class);

    private static final boolean LOG = false;

    private final String mCatalogId;
    private final String mViewSession;
    private final boolean mLandscape;
    private final int[] mPages;
    private long mViewStart = 0;
    private long mZoomStart = 0;
    private long mZoomAccumulated = 0;

    public PageStat(String catalogId, String viewSessionUuid, int[] pages, boolean landscape) {
        mCatalogId = catalogId;
        mViewSession = viewSessionUuid;
        mPages = pages;
        mLandscape = landscape;
    }

    public void collectView() {
        if (mViewStart != 0) {
            log("viewCollect");
            collectZoom();
            long now = System.currentTimeMillis();
            long duration = (now - mViewStart) - mZoomAccumulated;
            collect(true, duration);
        }
        mViewStart = 0;
    }

    public void startView() {
        if (mViewStart == 0) {
            log("viewStart");
            mViewStart = System.currentTimeMillis();
            mZoomAccumulated = 0;
        }
    }

    public void startZoom() {
        if (mZoomStart == 0) {
            log("zoomStart");
            mZoomStart = System.currentTimeMillis();
        }
    }

    public void collectZoom() {
        if (mZoomStart != 0) {
            log("zoomCollect");
            long duration = System.currentTimeMillis() - mZoomStart;
            mZoomAccumulated += duration;
            collect(false, duration);
        }
        mZoomStart = 0;
    }

    private void collect(boolean isView, long duration) {

        final JSONObject body = getCollectData(isView, duration);
        String url = Api.Endpoint.catalogCollect(mCatalogId);
        JsonObjectRequest r = new JsonObjectRequest(Method.POST, url, body, new Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
//				print(body, response, error);
            }
        });
        ShopGun.getInstance().add(r);

    }

    private JSONObject getCollectData(boolean isView, long ms) {
        JSONObject o = new JSONObject();
        try {
            o.put("type", isView ? "view" : "zoom");
            o.put("ms", ms);
            o.put("orientation", mLandscape ? "landscape" : "portrait");
            o.put("pages", PageflipUtils.join(",", mPages));
            o.put("view_session", mViewSession);
        } catch (JSONException e) {
            SgnLog.d(TAG, e.getMessage(), e);
        }
        return o;
    }

    private void log(String s) {
        if (LOG) {
            SgnLog.d(TAG, "[" + PageflipUtils.join("-", mPages) + "] " + s);
        }
    }

}
