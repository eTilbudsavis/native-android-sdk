package com.eTilbudsavis.sdkdemo;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	Button btnPageflip;
	Button btnTesting;
	Button btnWeinre;
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

        btnWeinre = (Button)findViewById(R.id.btnWeinre);
        
        btnWeinre.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent i = new Intent(Main.this, WeinreWebView.class);
				startActivity(i);
			}
		});
        
        tvDebug = (TextView)findViewById(R.id.tvDebug);
        tvDebug.setText("Ready for action");
        
//		testSimpleDateFormat(5);
		
    }
    
    private void testSimpleDateFormat(final int iterations) { 
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				int i = 1;
		    	for (int j = 1 ; j < iterations+1 ; j++) {
		            long s = System.currentTimeMillis();
		            doWork(i);
		            Utils.logd(TAG, "Completed: " + String.valueOf(j) + " of " + String.valueOf(iterations) + " - iterations: " + String.valueOf(i) + " in " + String.valueOf(System.currentTimeMillis() - s) + "ms");
		            
		    		i = i*2;
		    	}
		    	h.post(new Runnable() {
					
					@Override
					public void run() {
						tvDebug.setText("thread done");
					}
				});
			}
		}).start();
    	
    }
    
    private void doWork(int iterations){

    	
        for (int i = 0 ; i < iterations ; i++) {
        	SimpleDateFormat sdf = new SimpleDateFormat(Utils.DATE_FORMAT);
        }
    }
    
}
