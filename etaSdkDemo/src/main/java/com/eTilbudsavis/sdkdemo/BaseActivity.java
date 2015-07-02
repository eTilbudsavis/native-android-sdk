package com.eTilbudsavis.sdkdemo;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.eTilbudsavis.etasdk.Eta;

public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /* 
         * Eta.create(Context ctx) must be invoked once, to instantiate the SDK
         * prior to calling Eta.getInstance().
         * 
         * Calling Eta.create(Context ctx) can also be called from Application.onCreate().
         * 
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        if (!Eta.isCreated()) {

            // Create your instance of Eta
            Eta.create(this);
			
			/* You can optionally set a develop flag. 
			 * I'm using BuildConfig, but you can choose what ever scheme you want.
			 */
            Eta.getInstance().setDevelop(BuildConfig.DEBUG);
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Eta.getInstance().onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Eta.getInstance().onStop();
    }
}
