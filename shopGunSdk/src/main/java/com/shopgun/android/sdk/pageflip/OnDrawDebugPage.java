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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Hotspot;

import java.util.List;

public class OnDrawDebugPage implements OnDrawPage {

    public static final String TAG = OnDrawDebugPage.class.getSimpleName();

    @Override
    public Bitmap onDraw(Catalog catalog, int page, int[] pages, Bitmap b) {

        List<Hotspot> hotspots = catalog.getHotspots().get(page);

        if (hotspots != null && !hotspots.isEmpty()) {

            if (!b.isMutable()) {
                // Memory inefficient but need
                Bitmap tmp = b.copy(Bitmap.Config.RGB_565, true);
                b.recycle();
                b = tmp;
            }

            Canvas c = new Canvas(b);

            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);

            for (Hotspot h : hotspots) {
                if (h.isAreaSignificant(pages)) {
                    p.setColor(h.getColor());
                    c.drawRect(h.getRect(b), p);
                }
            }

        }

        return b;
    }
}
