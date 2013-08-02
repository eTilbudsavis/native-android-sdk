package com.eTilbudsavis.sdkdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.etilbudsavis.sdkdemo.R;

public class WeinreWebView  extends Activity {


    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weinre);
        WebView vw = (WebView)findViewById(R.id.webView1);
        vw.getSettings().setJavaScriptEnabled(true);
        vw.loadUrl("http://eta.dannyhvam.dk/");
        
    }
    
	
}
