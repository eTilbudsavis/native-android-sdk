/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.imageloader;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.EtaThreadFactory;

/**
 * Interface that allows pre/post processing of Bitmaps while the ImageLoader
 * is performing the request. This method will be called from a thread in the
 * ExecutorService pool, so by using {@link EtaThreadFactory} the interface
 * will be running on a low-priority thread.
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface BitmapProcessor {

    /**
     * This method will be called as soon as the {@link ImageLoader} has loaded
     * the bitmap. And you can then perform any action on the Bitmap.
     *
     * @param b A Bitmap that have been loaded by the {@link ImageLoader}
     * @return A new bitmap, that have been processed
     */
    public Bitmap process(Bitmap b);

}
