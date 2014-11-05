package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.net.URLEncoder;

import com.eTilbudsavis.etasdk.ImageLoader.FileNameGenerator;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultFileName implements FileNameGenerator {

	public String getFileName(ImageRequest ir) {
		return getName(ir.getUrl());
	}

	@SuppressWarnings("deprecation")
	public static String getName(String url) {
		return URLEncoder.encode(url);
	}
	
}
