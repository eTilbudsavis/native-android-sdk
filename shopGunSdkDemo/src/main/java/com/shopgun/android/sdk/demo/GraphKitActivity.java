package com.shopgun.android.sdk.demo;

import android.app.Activity;
import android.os.Bundle;

import com.shopgun.android.sdk.graphkit.GraphKit;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GraphKitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphkit);

        Call call = GraphKit.getInstance().newRequest(" query: {}");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

}
