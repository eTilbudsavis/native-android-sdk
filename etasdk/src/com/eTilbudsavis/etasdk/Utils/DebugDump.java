package com.eTilbudsavis.etasdk.Utils;

import java.util.LinkedList;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;

import com.eTilbudsavis.etasdk.Eta;

public class DebugDump extends AsyncTask<Void, Void, Void> {

	public static final String TAG = "DebugDump";
	
	public static String DEBUG_URL = null;
	private LinkedList<NameValuePair> mQuery = new LinkedList<NameValuePair>();
	
	public DebugDump(String exception, String data) {
		
		if (DEBUG_URL == null) {
			return;
		}
		
		try {
			String appVersion = Eta.getInstance().getAppVersion();
			mQuery.add(new BasicNameValuePair("version", (appVersion == null ? "null" : appVersion) ));
			mQuery.add(new BasicNameValuePair("exception", exception));
			mQuery.add(new BasicNameValuePair("model", Device.getModel()));
			mQuery.add(new BasicNameValuePair("android", Device.getBuildVersion()));
			mQuery.add(new BasicNameValuePair("baseband", Device.getRadio()));
			mQuery.add(new BasicNameValuePair("kernel", Device.getKernel()));
			mQuery.add(new BasicNameValuePair("data", data));
		} catch (Exception e) {
			
		}
		
	}

	@Override
	protected Void doInBackground(Void... params) {

		if (DEBUG_URL == null) {
			return null;
		}
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		try {
			HttpPost post = new HttpPost(DEBUG_URL);
			post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));
			httpClient.execute(post);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		
	}
	
}

