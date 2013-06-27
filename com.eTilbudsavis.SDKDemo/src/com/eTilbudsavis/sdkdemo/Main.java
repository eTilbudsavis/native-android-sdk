package com.eTilbudsavis.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button pageflip = (Button)findViewById(R.id.btnPageflip);
        
        pageflip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent i = new Intent(Main.this, CatalogViewer.class);
				startActivity(i);
			}
		});
        
        Button testing = (Button)findViewById(R.id.btnTesting);
        
        testing.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, Testing.class);
				startActivity(i);
			}
		});
        
    }
    
}
