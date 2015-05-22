/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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
 ******************************************************************************/
package com.eTilbudsavis.sdkdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaThreadFactory;
import com.eTilbudsavis.etasdk.database.DatabaseWrapper;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Offer;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.test.ModelCreator;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.utils.Api.Param;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBSpeedTestActivity extends BaseActivity {
	
	public static final String TAG = DBSpeedTestActivity.class.getSimpleName();
	
	public static final String ARG_OFFERS = "offers";
	public static final String ARG_QUERY = "query";
	
	Button mButton;
	TextView mTextView;
	String mText = "";
	boolean mTesting = false;

	DatabaseWrapper mDatabase;
	ExecutorService mExecutor = Executors.newFixedThreadPool(1);

	Runnable mDrawUI = new Runnable() {
		@Override
		public void run() {
			mTextView.setText(mText + (mTesting ? "\nRunning..." : "Done..."));
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbspeedtest);

        // Find views
        mButton = (Button) findViewById(R.id.button);
		mTextView = (TextView) findViewById(R.id.textView);

		mDatabase = DatabaseWrapper.getInstance(getApplicationContext());

		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				runTest();
			}
		});
        
    }

	private void runTest() {

		// Dont do this!
		mTesting = true;
		mExecutor.execute(new DbTest(1000));

	}

	public class DbTest implements Runnable {

		int mTestSize = 1000;
		Shoppinglist mList;
		List<ShoppinglistItem> mItems;
		User mUser;

		public DbTest() {
			this(100);
		}

		public DbTest(int mTestSize) {
			this.mTestSize = mTestSize;
			this.mList = Shoppinglist.fromName("testlist");
			this.mItems = new ArrayList<ShoppinglistItem>(mTestSize);
			this.mUser = ModelCreator.getUser(1980, "me@me.com", "male", "me", ModelCreator.getPermission(), User.NO_USER);
		}

		@Override
		public void run() {

			mItems = getItems(mTestSize, mList, mUser);

			logCount("Test size", mTestSize);
			logTime("db.clear", prep());
			logTime("list.insert", insertList());
			logTime("item.insert.slow", insertItemsSlow(mItems, mUser));
			logCount("item.inserted.count", getItems(mList, mUser).size());

			logTime("db.clear", prep());
			logTime("list.insert", insertList());
			logTime("Insert ItemFast", insertItemsFast(mItems, mUser));
			logCount("Items inserted", getItems(mList, mUser).size());

			logTime("db.clear", prep());
			mTesting = false;
			runOnUiThread(mDrawUI);

		}

		private long prep() {
			long s = System.currentTimeMillis();
			mDatabase.clear();
			return System.currentTimeMillis() - s;
		}

		private long insertList() {
			long s = System.currentTimeMillis();
			mDatabase.insertList(mList, mUser);
			return System.currentTimeMillis() - s;
		}

		private List<ShoppinglistItem> getItems(int size, Shoppinglist sl, User u) {
			List<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
			for (int i = 0; i < mTestSize; i++) {
				ShoppinglistItem sli = generateItem(i, mList, mUser);
				list.add(sli);
			}
			return list;
		}

		private long insertItemsSlow(List<ShoppinglistItem> items, User u) {
			long s = System.currentTimeMillis();
			for (ShoppinglistItem sli : items) {
				mDatabase.insertItem(sli, u);
			}
			return System.currentTimeMillis() - s;
		}

		private long insertItemsFast(List<ShoppinglistItem> items, User u) {
			long s = System.currentTimeMillis();
			mDatabase.insertItems(items, u);
			return System.currentTimeMillis() - s;
		}

		private List<ShoppinglistItem> getItems(Shoppinglist sl, User u) {
			return mDatabase.getItems(sl, u);
		}
	}

	private ShoppinglistItem generateItem(int i, Shoppinglist sl, User u) {
		ShoppinglistItem s = new ShoppinglistItem(sl, String.valueOf(i));
		s.setCreator(u.getEmail());
		s.setUserId(u.getUserId());
		return s;
	}

	private void logTime(String msg, long time) {
		String s = msg + ": " + time + "ms";
		mText += s + "\n";
		Log.d(TAG, s);
		runOnUiThread(mDrawUI);
	}

	private void logCount(String msg, int count) {
		String s = msg + ": " + count;
		mText += s + "\n";
		Log.d(TAG, s);
		runOnUiThread(mDrawUI);
	}

}
