package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eTilbudsavis.etasdk.ImageLoader.ImageDownloader;

public class DefaultImageDownloader implements ImageDownloader {
	
	public static final String TAG = ImageDownloader.class.getSimpleName();
	
	private static final int BUFFER_SIZE = 0x10000;
	
	public Bitmap getBitmap(String url) throws IllegalStateException, IOException, OutOfMemoryError {
		
		URL imageUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setInstanceFollowRedirects(true);
		byte[] image = entityToBytes(conn);
		return BitmapFactory.decodeByteArray(image, 0, image.length);
		
	}
	
	private static byte[] entityToBytes(HttpURLConnection connection) throws IOException {
		
		int init_buf = ( 0 <= connection.getContentLength() ? (int)connection.getContentLength() : BUFFER_SIZE );
		ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
		InputStream is = connection.getInputStream();
		
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
