package com.etilbudsavis.sdkdemo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CatalogInfo extends Activity {

	private JSONObject extras;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.catalog_info);

		TextView dealerName = (TextView)findViewById(R.id.textViewDealer);
		TextView catID = (TextView)findViewById(R.id.textViewCatalogEdit);
		TextView pages = (TextView)findViewById(R.id.textViewPagesEdit);
		TextView address = (TextView)findViewById(R.id.textViewAddressEdit);
		Button btn = (Button)findViewById(R.id.button1);
		
		try {
			extras = new JSONObject(getIntent().getExtras().getString("JSONObject"));
			dealerName.setText(extras.getJSONObject("dealer").getString("name"));
			catID.setText(extras.getString("id"));
			pages.setText(extras.getString("pages"));
			address.setText(extras.getJSONObject("store").getString("street") +
					"\r\n" + 
					extras.getJSONObject("store").getString("city") );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(extras.getString("url")));
					startActivity(viewIntent); 
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

}