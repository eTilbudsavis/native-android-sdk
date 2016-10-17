package com.shopgun.android.sdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.shopgun.android.sdk.graphkit.GraphRequest;
import com.shopgun.android.utils.log.L;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GraphKitActivity extends Activity {

    public static final String TAG = GraphKitActivity.class.getSimpleName();

    TextView mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphkit);
        mInfo = (TextView) findViewById(R.id.info);

        Call call = GraphRequest.newCall("query: {}");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                main("onFailure: " + call.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                main("onResponse: " + response.toString());
            }

            private void main(final String text) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mInfo.setText(text);
                        L.d(TAG, text);
                    }
                });
            }
        });

    }

}
