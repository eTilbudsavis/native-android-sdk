package com.eTilbudsavis.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	Button btnPageflip;
	Button btnTesting;
	TextView tvDebug;
	Handler h = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnPageflip= (Button)findViewById(R.id.btnPageflip);
        
        btnPageflip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent i = new Intent(Main.this, CatalogViewer.class);
				startActivity(i);
			}
		});

        btnTesting = (Button)findViewById(R.id.btnTesting);
        
        btnTesting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent i = new Intent(Main.this, Testing.class);
				startActivity(i);
			}
		});

        tvDebug = (TextView)findViewById(R.id.tvDebug);
        tvDebug.setText("Ready for action");
        
    }
    
}
