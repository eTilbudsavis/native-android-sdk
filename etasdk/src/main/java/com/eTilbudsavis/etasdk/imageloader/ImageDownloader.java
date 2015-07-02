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

import java.io.IOException;

/**
 * Interface for downloading images from web.
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface ImageDownloader {

    /**
     * This method should return a bitmap from the requested url.
     *
     * @param url An url pointing at the imageresource to download
     * @return A bitmap, or {@code null}
     * @throws IllegalStateException
     * @throws IOException
     * @throws OutOfMemoryError
     */
    public Bitmap getBitmap(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError;

    /**
     * This method should return a bitmap from the requested url.
     *
     * @param url An url pointing at the imageresource to download
     * @return A bitmap, or {@code null}
     * @throws IllegalStateException
     * @throws IOException
     * @throws OutOfMemoryError
     */
    public byte[] getByteArray(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError;

}
