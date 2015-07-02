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

package com.eTilbudsavis.etasdk.imageloader.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.FileNameGenerator;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.utils.HashUtils;

public class DefaultFileName implements FileNameGenerator {

    public static final String TAG = Constants.getTag(DefaultFileName.class);

    public String getFileName(ImageRequest ir) {
        return HashUtils.md5(ir.getUrl());
    }

//	@SuppressWarnings("deprecation")
//	private static String getName(String url) {
//		return URLEncoder.encode(url);
//	}

}
