package com.shopgun.android.sdk.graphkit;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.utils.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GraphRequest {

    private static final String URL = "https://graph-staging.shopgun.com";
    private static final HttpUrl URL_PARSED = HttpUrl.parse(URL);
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Headers HEADERS = new Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .build();

    public static Call newCall(String query) {
        return newCall(ShopGun.getInstance().getClient(), query);
    }

    public static Call newCall(OkHttpClient client, String query) {
        return newCall(client, query, null, null);
    }

    public static Call newCall(OkHttpClient client, String query, String operationName, String[] variables) {

        Map<String, String> map = new HashMap<>();
        map.put("query", query);
        map.put("operationName", operationName);
        map.put("variables", variables == null ? null : TextUtils.join(",", variables));

        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, new JSONObject(map).toString());

        Request request = new Request.Builder()
                .url(URL_PARSED)
                .headers(HEADERS)
                .post(requestBody)
                .build();

        return client.newCall(request);

    }


}
