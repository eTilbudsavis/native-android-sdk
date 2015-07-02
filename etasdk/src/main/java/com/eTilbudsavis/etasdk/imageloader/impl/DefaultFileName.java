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
