package com.shopgun.android.sdk.corekit;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SessionRetryInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();

        // try the request
        Response response = chain.proceed(request);

        int tryCount = 0;
        while (!response.isSuccessful() && tryCount < 3) {

            Log.d("intercept", "Request is not successful - " + tryCount);

            tryCount++;

            // retry the request
            response = chain.proceed(request);
        }

        // otherwise just pass the original response on
        return response;

    }

}
