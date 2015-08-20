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

package com.shopgun.android.sdk.demo;

import android.util.Log;
import android.view.View;

import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.pageflip.PageflipListener;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import java.util.List;

public class PageflipListenerPrinter implements PageflipListener {

    private String mTag;
    private boolean mPrint = false;

    public PageflipListenerPrinter(String mTag, boolean mPrint) {
        this.mTag = mTag;
        this.mPrint = mPrint;
    }

    @Override
    public void onReady() {
        log("onReady");
    }

    @Override
    public void onPageChange(int[] pages) {
        log("onPageChange: " + PageflipUtils.join(",", pages));
    }

    @Override
    public void onOutOfBounds(boolean left) {
        log("onOutOfBounds: " + (left ? "left" : "right"));
    }

    @Override
    public void onDragStateChanged(int state) {
        log("onDragStateChanged");
    }

    @Override
    public void onError(ShopGunError error) {
        log("onError: " + error.toString());
    }

    @Override
    public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
        log("onSingleClick " + hs(hotspots));
    }

    @Override
    public void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
        log("onDoubleClick " + hs(hotspots));
    }

    @Override
    public void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
        log("onLongClick " + hs(hotspots));
    }

    @Override
    public void onZoom(View v, int[] pages, boolean zoomIn) {
        log("onZoom: " + (zoomIn ? "zoom_in" : "zoom_out"));
    }

    private String hs(List<Hotspot> hotspots) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hotspots[");
        for (Hotspot h : hotspots) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(h.getOffer().getHeading());

        }
        sb.append("]");
        return sb.toString();
    }

    private void log(String msg) {
        if (mPrint) {
            Log.d(mTag, msg);
        }
    }
}
