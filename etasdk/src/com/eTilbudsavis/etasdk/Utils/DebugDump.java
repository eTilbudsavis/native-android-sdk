package com.eTilbudsavis.etasdk.Utils;

import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;

import com.eTilbudsavis.etasdk.Eta;

/**
 * A class used for dumping random debug data, to an endpoint that isn't yet specified.
 * @author Danny Hvam
 *
 */
public class DebugDump extends AsyncTask<Void, Void, Integer> {

	public static final String TAG = "DebugDump";
	
	private String mUrl;
	private LinkedList<NameValuePair> mQuery = new LinkedList<NameValuePair>();
	private OnCompleteListener mListener;
	
	public DebugDump(String url, String exception, String data) {
		
		mUrl = url;
		
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
			e.printStackTrace();
		}
		
	}
	
	public DebugDump(String url, String exception, String data, OnCompleteListener l) {
		this(url, exception, data);
		mListener = l;
	}
	
	@Override
	protected Integer doInBackground(Void... urls) {
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		Integer sc = null;
		try {
			HttpPost post = new HttpPost(mUrl);
			post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));
			HttpResponse resp = httpClient.execute(post);
			sc = resp.getStatusLine().getStatusCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sc;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (mListener != null) {
			int sc = result == null ? -1 : result;
			mListener.onComplete(sc);
		}
	}
	
	public interface OnCompleteListener {
		public void onComplete(int statusCode);
	}
	
}

