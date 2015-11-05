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

import android.graphics.Bitmap;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dimension;
import com.squareup.picasso.Transformation;

/**
 * This transformation is designed to crop the excess white edges from the {@link Catalog} thumbnails.
 */
public class CatalogThumbTransformation implements Transformation {

    public static final String TAG = CatalogThumbTransformation.class.getSimpleName();

    private static final float EPSILON = 0.001f;

    private Catalog mCatalog;

    public CatalogThumbTransformation(Catalog mCatalog) {
        this.mCatalog = mCatalog;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        if (mCatalog == null) {
            return source;
        }

        Dimension d = mCatalog.getDimension();
        if (d == null || d.getWidth() == null || d.getHeight() == null) {
            // The API can return a null Dimension, and null width/height
            return source;
        }

        double dimenWidth = d.getWidth();
        double dimenHeight = d.getHeight();
        int w = source.getWidth();
        int h = source.getHeight();
        int x = 0;
        int y = 0;

        float dimenRatio = (float)dimenHeight / (float)dimenWidth;
        float sourceRatio = (float)h / (float)w;

        if (Math.abs(dimenRatio-sourceRatio) < EPSILON) {
            // Close enough, lets save memory
            return source;
        } else if (dimenHeight > sourceRatio) {
            // cut some from the width
            int tmp = (int)(h * (dimenWidth / dimenHeight));
            x = Math.abs(w-tmp)/2;
            w = tmp;
        } else {
            // cut height
            int tmp = (int)(w * (dimenHeight / dimenWidth));
            y = Math.abs(h-tmp)/2;
            h = tmp;
        }

        try {
            Bitmap b = Bitmap.createBitmap(source, x, y, w, h);
            source.recycle();
            return b;
        } catch (Exception e) {
            SgnLog.e(TAG, "Unable to create a new Bitmap", e);
            return source;
        }
    }

    @Override
    public String key() {
        return mCatalog.getErn() + "-image-thumb-crop";
    }

}
