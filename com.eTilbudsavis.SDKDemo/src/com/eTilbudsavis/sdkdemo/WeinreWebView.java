package com.eTilbudsavis.sdkdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class WeinreWebView  extends Activity {

	WebView vw;
	WebChromeClient wcc = new WebChromeClient() {
			
			@Override
			public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {

				new AlertDialog.Builder(WeinreWebView.this)  
	            .setTitle("JavaScript Alert")  
	            .setMessage(message)  
	            .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() { 
	            	public void onClick(DialogInterface dialog, int which) { result.confirm(); } })
	            .setCancelable(false)
	            .create()
	            .show();
				
	        return true;
	        
			}
		};
		
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weinre);
        vw = (WebView)findViewById(R.id.webView1);
        vw.clearCache(true);
        vw.getSettings().setJavaScriptEnabled(true);
        vw.setWebChromeClient(wcc);
        vw.loadUrl("http://192.168.1.119:3000/catalogs/93f13/851bCRg/");
//        vw.loadUrl("http://eta.dannyhvam.dk/");
        
    }
    
}
