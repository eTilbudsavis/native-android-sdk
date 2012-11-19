package com.etilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import com.etilbudsavis.etasdk.API.RequestListener;
import android.annotation.TargetApi;
import android.os.AsyncTask;

@TargetApi(3)
public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	private String mUrl;
	private String mQuery;
	private API.RequestType mRequestType;
	private API.AcceptType mAcceptType;
	private RequestListener mRequestListener;
	private String mResult = "";
	private int mResponseCode = 0;

	// Constructor for HttpHelper.
	public HttpHelper(String url, String query,
			API.RequestType requestType, API.AcceptType acceptType,
			RequestListener requestListener) {
		mUrl = (requestType == API.RequestType.GET) ? url + "?"+ query : url;
		mQuery = query;
		mRequestType = requestType;
		mAcceptType = acceptType;
		mRequestListener = requestListener;
	}

	@Override
	protected Void doInBackground(Void... params) {
		StringBuilder sb = new StringBuilder();

		try {	
			// Create new URL.
			URL serverUrl = new URL(mUrl);
			
			// Open new http connection and setup headers.
			HttpsURLConnection connection = (HttpsURLConnection) serverUrl.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod(mRequestType.toString());
			connection.setRequestProperty("Accept", mAcceptType.toString());
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setUseCaches(false);

			// If POST request, do output stream.
			if (mRequestType == API.RequestType.POST) {
				connection.setRequestProperty("Content-Length", "" + Integer.toString(mQuery.getBytes().length));
				DataOutputStream wr;
				wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(mQuery);
				wr.flush();
				wr.close();
			}
			
			// Read the input stream (HTML or data)
			BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}

			rd.close();
						
			// Store results, so they can be used by onPostExecute in UI thread.
			mResult = sb.toString();
			mResponseCode = connection.getResponseCode();

			connection.disconnect();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return null;
	}
	
	// Do callback in the UI thread
	@Override
	protected void onPostExecute(Void result) {
		if (mResponseCode == 200) mRequestListener.onSuccess("Success", mResult);
		else mRequestListener.onError("Error", mResult);
    }
	
}