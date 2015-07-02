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

/**
 * Interface for caching bitmaps to memory.
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface MemoryCache {

    /**
     * Method being called to save a {@link ImageRequest} to memory (the bitmap actually)
     *
     * @param id An identifier for the bitmap
     * @param b  A bitmap to cache
     */
    public void put(String id, Bitmap b);

    /**
     * Method for getting bitmaps from cache.
     *
     * @param id An identifier to search for in cache
     * @return A bitmap if one exists, else {@code null}
     */
    public Bitmap get(String id);

    /**
     * Method for clearing the cache completely
     */
    public void clear();

}