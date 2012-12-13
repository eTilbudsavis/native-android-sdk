package com.etilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.etilbudsavis.etasdk.API.RequestListener;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.webkit.WebView;

@TargetApi(3)
public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	private String mUrl;
	private String mQuery;
	private API.RequestType mRequestType;
	private API.AcceptType mAcceptType;
	private RequestListener mRequestListener;
	private String mResult = "";
	private String mResponseCode = "";

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
			HttpURLConnection connection = (HttpURLConnection) serverUrl.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestMethod(mRequestType.toString());
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Accept", mAcceptType.toString());
			connection.setUseCaches(false);

			// If POST request, do output stream.
			if (mRequestType == API.RequestType.POST) {
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Length", "" + String.valueOf(mQuery.getBytes().length));
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(connection.getOutputStream(),
								"UTF-8"));
				writer.write(mQuery);
				writer.close();
			}

			// Read the input stream (HTML or data)
			BufferedReader reader = new BufferedReader(new InputStreamReader( 
					connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();

			// Store results, so they can be used by onPostExecute in UI thread.
			mResult = sb.toString().length() == 0 ? "" : sb.toString();
			mResponseCode = String.valueOf(connection.getResponseCode());

			connection.disconnect();
		} catch (IOException e) {
			mResponseCode = "IO Error";
			e.printStackTrace();
		}

		return null;
	}
	
	// Do callback in the UI thread
	@Override
	protected void onPostExecute(Void result) {
		if (mResponseCode.matches("200")) mRequestListener.onSuccess(mResponseCode, mResult);
		else mRequestListener.onError(mResponseCode, mResult);
    }
	
}