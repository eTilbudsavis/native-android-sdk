package com.eTilbudsavis.sdkdemo;

import android.app.Application;

import com.eTilbudsavis.etasdk.Eta;

public class App extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Eta.createInstance(Keys.API_KEY, Keys.API_SECRET, getApplicationContext());
	}
	
}
