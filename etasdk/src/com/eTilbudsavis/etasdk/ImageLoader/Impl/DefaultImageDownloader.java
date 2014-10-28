package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.ImageDownloader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultImageDownloader implements ImageDownloader {
	
	public static final String TAG = Eta.TAG_PREFIX + ImageDownloader.class.getSimpleName();
	
	private static final int BUFFER_SIZE = 0x10000;
	private static final int TIMEOUT = 20000;
	
	public Bitmap getBitmap(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError {
		
		URL imageUrl = new URL(ir.getUrl());
		HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
		conn.setConnectTimeout(TIMEOUT);
		conn.setReadTimeout(TIMEOUT);
		conn.setInstanceFollowRedirects(true);
//		byte[] image = entityToBytes(conn);
		Bitmap b = ir.getBitmapDecoder().decode(ir, conn.getInputStream());
		return b;
	}
	
	private static byte[] entityToBytes(HttpURLConnection connection) throws IOException {
		
		int init_buf = ( 0 <= connection.getContentLength() ? (int)connection.getContentLength() : BUFFER_SIZE );
		
		ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
		InputStream is = connection.getInputStream();
//		EtaLog.d(TAG, "InputStream: " + is.getClass().getSimpleName());
		if (is != null) {
			
			byte[] buf = new byte[init_buf];
			int c = -1;
			while ( (c=is.read(buf)) != -1) {
				bytes.append(buf, 0, c);
			}
			
		}
		
		return bytes.toByteArray();
	}
	
}
