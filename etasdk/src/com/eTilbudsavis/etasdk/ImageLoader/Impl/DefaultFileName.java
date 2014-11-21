package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.FileNameGenerator;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class DefaultFileName implements FileNameGenerator {
	
	public static final String TAG = Eta.TAG_PREFIX + DefaultFileName.class.getSimpleName();
	
	public String getFileName(ImageRequest ir) {
		return md5(ir.getUrl());
	}

	@SuppressWarnings("deprecation")
	private static String getName(String url) {
		return URLEncoder.encode(url);
	}
	
	private String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	        
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	    	EtaLog.e(TAG, e.getMessage(), e);
	    }
	    return "";
	}
	
}
