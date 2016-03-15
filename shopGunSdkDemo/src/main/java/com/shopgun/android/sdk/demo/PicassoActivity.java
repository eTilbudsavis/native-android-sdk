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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogListRequest;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.UrlConnectionDownloader;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PicassoActivity extends Activity {

    public static final String TAG = PicassoActivity.class.getSimpleName();

    @Bind(R.id.picasso_downloader)
    TextView mTextViewDownloader;

    @Bind(R.id.picasso_progress)
    TextView mTextViewProgress;

    @Bind(R.id.picasso_url)
    TextView mTextViewUrl;

    @Bind(R.id.radio)
    RadioGroup mRadioGroup;

    @Bind(R.id.latency)
    EditText mLatency;

    Picasso mPicasso;
    Downloader mDownloader;
    List<String> mImages = new ArrayList<>();
    List<Target> mTargets = new ArrayList<>();
    int mDone = 0;
    long mStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picasso);
        ButterKnife.bind(this);

        onClickChangeDownloader();
    }

    @OnClick(R.id.download_images)
    protected void onClickDownloadImages() {

        mImages.clear();
        int latency = 0;
        try {
            latency = Integer.valueOf(mLatency.getText().toString());
        } catch (NumberFormatException e) {

        }

        switch (mRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radioAkamai: getCatalogList(); break;
            case R.id.radioHttp1: getGolangThumbs(true, latency); break;
            case R.id.radioHttp2: getGolangThumbs(false, latency); break;
        }

    }
    
    private void getGolangThumbs(boolean http1, int latency) {
        String format = (http1 ? "http" : "https") + "://http2.golang.org/gophertiles?x=%s&y=%s&cachebust=" + System.currentTimeMillis() + "&latency=" + latency;
        for (int i = 0; i < 180; i++) {
            int x = i%15;
            int y = (int)((float)i/(float)15);
            mImages.add(String.format(format, x, y));
        }
        runPicasso();
    }
    
    private void getCatalogList() {

        mTextViewProgress.setText("Getting catalogs...");
        CatalogListRequest request = new CatalogListRequest(new LoaderRequest.Listener<List<Catalog>>() {
            @Override
            public void onRequestComplete(List<Catalog> response, List<ShopGunError> errors) {

                if (errors.isEmpty()) {

                    for (Catalog c : response) {
                        for (Images i : c.getPages()) {
                            mImages.add(i.getThumb());
                        }
                        if (mImages.size() > 200) {
                            break;
                        }
                    }
                    runPicasso();

                } else {
                    mTextViewProgress.setText("Catalogs failed");
                }

            }

            @Override
            public void onRequestIntermediate(List<Catalog> response, List<ShopGunError> errors) {

            }
        });
        request.loadPages(true);
        ShopGun.getInstance(this).add(request);

    }

    @OnClick(R.id.change_downloader)
    protected void onClickChangeDownloader() {

        if (mDownloader == null || mDownloader instanceof OkHttpDownloader) {
            mDownloader = new UrlConnectionDownloader(this);
        } else {
//            OkHttpClient client = new OkHttpClient();
//            client.setProtocols(Arrays.asList(Protocol.HTTP_1_1,Protocol.HTTP_2));
//            mDownloader = new OkHttpDownloader(client);
            mDownloader = new OkHttpDownloader(this);
        }
        mTextViewDownloader.setText(mDownloader.getClass().getSimpleName());
        mPicasso = new Picasso.Builder(this)
                .loggingEnabled(false)
                .downloader(mDownloader)
                .build();
    }

    private float getPercentage() {
        return (( ((float) mDone) / ((float) mImages.size()) )*100f);
    }

    private void runPicasso() {

        mDone = 0;
        mStart = System.currentTimeMillis();
        mTargets.clear();
        mTextViewUrl.setText(mImages.get(0));

        for (final String pageUrl : mImages) {

            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mDone++;
                    if (mDone == mImages.size()) {
                        long time = System.currentTimeMillis() - mStart;
                        mTextViewProgress.setText(String.format("Finished in %sms", time));
                    } else {
                        mTextViewProgress.setText(String.format("Progress: %.0f%%", getPercentage()));
                    }

                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mDone++;
                    mTextViewProgress.setText(String.format("onBitmapFailed, progress: %.0f%%", getPercentage()));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
//                    log("onPrepareLoad");
                }
            };

            mTargets.add(target);

            mPicasso.load(pageUrl)
                    .networkPolicy(NetworkPolicy.NO_STORE, NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(target);

        }

    }

}
