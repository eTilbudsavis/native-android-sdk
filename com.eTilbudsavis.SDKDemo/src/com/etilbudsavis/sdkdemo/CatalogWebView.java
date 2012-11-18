package com.etilbudsavis.sdkdemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.etilbudsavis.etasdk.ETA;
import com.etilbudsavis.etasdk.Pageflip;
import com.etilbudsavis.etasdk.Pageflip.PageflipListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

public class CatalogWebView extends Activity {

	private WebView mWebView;
	private JSONObject mJObject;
	private ETA eta;
	private Pageflip pageflip;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pageflip);
		
		mWebView = (WebView)findViewById(R.id.webView1);

		String content = "";
		
		Intent i = getIntent();
		try {
			mJObject = new JSONObject(i.getExtras().getString("JSONObject"));
			content = mJObject.getString("id");
			eta = (ETA)i.getSerializableExtra("eta");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		pageflip = new Pageflip(mWebView, eta);
		
		mWebView = pageflip.getWebView(Pageflip.ContentType.CATALOG, content, pageflipListener);
	}

	// Remember to close pageflip when the activity is destroyed.
	@Override
	public void onStop() {
		pageflip.injectJS("pageflip.close();");
	}
	
	PageflipListener pageflipListener = new PageflipListener() {
		@Override
		public void onPageflipEvent(String event, JSONObject object) {
			if (event.matches("pagechange")) {
				try {
					Toast.makeText(getApplicationContext(), "Page: " + object.getString("page"), Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}	
		}
	};
	
}