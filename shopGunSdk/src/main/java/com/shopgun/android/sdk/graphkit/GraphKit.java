package com.shopgun.android.sdk.graphkit;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.Constants;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class GraphKit {

    public static final String TAG = Constants.getTag(GraphKit.class);

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";

    ShopGun mShopGun;
    OkHttpClient mClient;

    private GraphKit() {
        // private
    }

    public static GraphKit getInstance() {
        return null;
    }

    public Call newRequest(String query) {
        return GraphRequest.newCall(mClient, query);
    }

    public static class Builder {

        private OkHttpClient mClient;

        public Builder client(OkHttpClient client) {
            mClient = client;
            return this;
        }

        public Builder() {

        }

        public GraphKit build() {

            if (mClient == null) {
                mClient = new OkHttpClient.Builder().build();
            }

            return new GraphKit();
        }

    }

}
