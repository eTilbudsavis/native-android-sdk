package com.eTilbudsavis.etasdk.pageflip;

import com.eTilbudsavis.etasdk.ImageLoader.FileNameGenerator;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class PageflipFileNameGenerator implements FileNameGenerator {
	
	public String getFileName(ImageRequest ir) {
		return getName(ir.getUrl());
	}
	
	public static String getName(String url) {
		String s[] = url.split("/");
		int l = s.length-1;
		return s[l-1] + "-" + s[l];
	}
	
}
