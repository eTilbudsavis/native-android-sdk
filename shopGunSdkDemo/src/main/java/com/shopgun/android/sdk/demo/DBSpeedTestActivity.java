/*******************************************************************************
 * Copyright 2015 ShopGun
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

package com.shopgun.android.sdk.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.test.ModelCreator;

import java.util.ArrayList;
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
        mExecutor.execute(new DbTest(50, 20));

    }

    private long insertItemsSlow(DatabaseWrapper db, List<ShoppinglistItem> items, User u) {
        long s = System.currentTimeMillis();
        for (ShoppinglistItem sli : items) {
            db.insertItem(sli, u);
        }
        return System.currentTimeMillis() - s;
    }

    private long insertList(DatabaseWrapper db, Shoppinglist sl, User u) {
        long s = System.currentTimeMillis();
        db.insertList(sl, u);
        return System.currentTimeMillis() - s;
    }

    private long insertList(DatabaseWrapper db, List<Shoppinglist> list, User u) {
        long s = System.currentTimeMillis();
        db.insertLists(list, u);
        return System.currentTimeMillis() - s;
    }

    private long getListAll(DatabaseWrapper db, User u) {
        long s = System.currentTimeMillis();
        List<Shoppinglist> lists = db.getLists(u);
        return System.currentTimeMillis() - s;
    }

    private long insertItemsFast(List<ShoppinglistItem> items, User u) {
        long s = System.currentTimeMillis();
        mDatabase.insertItems(items, u);
        return System.currentTimeMillis() - s;
    }

    private long prep(DatabaseWrapper db) {
        long s = System.currentTimeMillis();
        db.clear();
        return System.currentTimeMillis() - s;
    }

    private ShoppinglistItem generateItem(int i, Shoppinglist sl, User u) {
        ShoppinglistItem s = new ShoppinglistItem(sl, String.valueOf(i));
        s.setCreator(u.getEmail());
        s.setUserId(u.getUserId());
        return s;
    }

    private void logTime(String msg, long time) {
        log(msg + ": " + time + "ms");
    }

    private void logCount(String msg, int count) {
        log(msg + ": " + count);
    }

    private void log(String s) {
        mText += s + "\n";
        Log.d(TAG, s);
        runOnUiThread(mDrawUI);
    }

    public class DbTest implements Runnable {

        final int mItemCount;
        final int mListCount;
        final User mUser;

        public DbTest() {
            this(50, 20);
        }

        public DbTest(int listCount, int itemCount) {
            mItemCount = itemCount;
            mListCount = listCount;
            mUser = ModelCreator.getUser(1980, "me@me.com", "male", "me", ModelCreator.getPermission(), User.NO_USER);
        }

        @Override
        public void run() {

            log(String.format("test[lists.size: %s, items.size: %s]", mListCount, mItemCount));

            List<Shoppinglist> lists = generateLists(mListCount, mUser);

            prep(mDatabase);

            long listInsertTime = 0;
            for (Shoppinglist sl : lists) {
                listInsertTime += insertList(mDatabase, sl, mUser);
            }
            logTime("list.insert.slow", listInsertTime);

            prep(mDatabase);

            logTime("list.insert.fast", insertList(mDatabase, lists, mUser));

            long itemInsertTime = 0;
            for (Shoppinglist sl : lists) {
                List<ShoppinglistItem> items = generateItems(mItemCount, sl, mUser);
                itemInsertTime += insertItemsSlow(mDatabase, items, mUser);
            }
            logTime("item.insert.slow", itemInsertTime);

            prep(mDatabase);

            logTime("list.insert.fast", insertList(mDatabase, lists, mUser));

            itemInsertTime = 0;
            for (Shoppinglist sl : lists) {
                List<ShoppinglistItem> items = generateItems(mItemCount, sl, mUser);
                itemInsertTime += insertItemsFast(items, mUser);
            }
            logTime("item.insert.fast", itemInsertTime);

            logTime("list.get.time", getListAll(mDatabase, mUser));

            mTesting = false;
            runOnUiThread(mDrawUI);

        }

        private List<Shoppinglist> generateLists(int size, User u) {
            List<Shoppinglist> lists = new ArrayList<Shoppinglist>();
            for (int i = 0; i < size; i++) {
                String idName = "list" + i;
                Shoppinglist sl = ModelCreator.getShoppinglist(idName, idName);
                // Model creator auto generates shares. We'll just remove them
//                sl.getShares().clear();
                Share share = ModelCreator.getShare(u.getEmail(), Share.ACCESS_OWNER, "eta.dk");
                sl.putShare(share);
                lists.add(sl);
            }
            return lists;
        }

        private List<ShoppinglistItem> generateItems(int size, Shoppinglist sl, User u) {
            List<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
            for (int i = 0; i < size; i++) {
                ShoppinglistItem sli = generateItem(i, sl, u);
                list.add(sli);
            }
            return list;
        }

        private List<ShoppinglistItem> generateItems(Shoppinglist sl, User u) {
            return mDatabase.getItems(sl, u);
        }
    }
}
