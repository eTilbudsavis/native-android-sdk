/*******************************************************************************
 * Copyright 2014 eTilbudsavis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.eTilbudsavis.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.eTilbudsavis.etasdk.log.DevLogger;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.test.EtaSdkTest;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int MENU_LOCATION = 1;

    private Button mBtnCatalogs;
    private Button mBtnSearch;
    private Button mBtnDbSpeedTest;
    private Button mBtnSDKUnitTest;

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnCatalogs:
                Intent cva = new Intent(MainActivity.this, CatalogViewerActivity.class);
                startActivity(cva);
                break;
            case R.id.btnSearch:
                Intent sa = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(sa);
                break;
            case R.id.btnSDKUnitTest:
                runSDKUnitTest();
                break;
            case R.id.btnDbSpeedTest:
                Intent dbsta = new Intent(MainActivity.this, DBSpeedTestActivity.class);
                startActivity(dbsta);
                break;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        EtaLog.setLogger(new DevLogger());
        
        /*
         * Eta is a singleton you interact with via this method
         */
        Eta eta = Eta.getInstance();
        
        /*
         * Set the location (This could also be set via LocationManager)
         */
        EtaLocation loc = eta.getLocation();
        loc.setLatitude(Constants.ETA_HQ.lat);
        loc.setLongitude(Constants.ETA_HQ.lng);

        // Avoid using large distances in production, it's bad for performance (longer queries)
        // the 700km radius here is just for demonstration purposes - we recommend 100km or less
        loc.setRadius(700000);
        loc.setSensor(false);
        
        /*
         * You are now done setting up the SDK, the rest is just Android stuff
         */
        mBtnCatalogs = (Button)findViewById(R.id.btnCatalogs);
        mBtnCatalogs.setOnClickListener(MainActivity.this);

        mBtnSearch = (Button) findViewById(R.id.btnSearch);
        mBtnSearch.setOnClickListener(MainActivity.this);

        mBtnDbSpeedTest = (Button)findViewById(R.id.btnDbSpeedTest);
        mBtnDbSpeedTest.setOnClickListener(MainActivity.this);

        mBtnSDKUnitTest = (Button) findViewById(R.id.btnSdkUnitTest);
        mBtnSDKUnitTest.setOnClickListener(MainActivity.this);

    }

    private void runSDKUnitTest() {
        Toast.makeText(MainActivity.this, "Running SDK Unit Test", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {

            @Override
            public void run() {
                EtaSdkTest.test();
                Toast.makeText(MainActivity.this, "SDK UnitTest completed successful", Toast.LENGTH_SHORT).show();
            }
        }).run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_LOCATION, Menu.NONE, "Choose location");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_LOCATION:
                Toast.makeText(MainActivity.this, "not implemented yet", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
