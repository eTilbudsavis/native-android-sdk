package com.shopgun.android.sdk.corekit;

import com.shopgun.android.sdk.SessionManager;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.utils.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiV2Interceptor implements Interceptor {

    public static final String TAG = ApiV2Interceptor.class.getSimpleName();
    AtomicBoolean isSessionMissing = new AtomicBoolean(true);
    CountDownLatch mLatch = new CountDownLatch(1);
    String sessionEndpoint = "http://oiznet.dk/";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (isV2Endpoint(request)) {
            log(request, "Intercepted V2 Request: " + request.url().toString());
            Response response = runRequest(chain, request);
            if (response.isSuccessful()) {
                updateHeaders(response.headers());
            } else {
                ShopGunError error = parseJsonError(response);
                if (SessionManager.recoverableError(error)) {
                    if (!isSessionEndpoint(request)) {
                        response = runRequest(chain, request);
                    }
                }
            }
            return response;
        }
        return chain.proceed(request);
    }

    private Response runRequest(Chain chain, Request request) throws IOException {
        latchAndFetchSession(request);
        request = applyTokenHeaders(request);
        return chain.proceed(request);
    }

    private boolean isV2Endpoint(Request request) {
        return request.url().host().contains("etilbudsavis.dk") &&
                request.url().pathSegments().get(0).contains("v2");
    }

    private boolean isSessionEndpoint(Request request) {
        List<String> path = request.url().pathSegments();
        return isV2Endpoint(request) && path.get(path.size()-1).contains("sessions");
    }

    private void updateHeaders(Headers headers) {
        if (headers != null) {
            String token = headers.get("X-Token");
            String expire = headers.get("X-Token-Expires");
            if (!(token == null || expire == null)) {
                ShopGun.getInstance().getSessionManager().updateTokens(token, expire);
            }
        }
    }

    private ShopGunError parseJsonError(Response response) throws IOException {
        ResponseBody body = response.body();
        try {
            JSONObject jsonError = new JSONObject(body.string());
            return ShopGunError.fromJSON(jsonError);
        } catch (JSONException e) {
            // ignore
        } finally {
            if (body != null) {
                body.close();
            }
        }
        return null;
    }

    /**
     *  If it's a post to sessions, it's to create a new Session, then the API key is needed.
     *  In any other case, just set the headers, with the current session token and signature.
     * @param request
     */
    private Request applyTokenHeaders(Request request) {
        boolean newSession = "POST".equalsIgnoreCase(request.method()) && isSessionEndpoint(request);
        if (!newSession) {
            ShopGun sgn = ShopGun.getInstance();
            String token = sgn.getSessionManager().getSession().getToken();
            String sha256 = HashUtils.sha256(sgn.getApiSecret() + token);
            HttpUrl url = request.url().newBuilder()
                    .addQueryParameter("X-Token", token)
                    .addQueryParameter("X-Signature", sha256)
                    .build();
            request = request.newBuilder().url(url).build();
        }
        return request;
    }

    private synchronized void latchAndFetchSession(Request request) throws IOException {

        // TODO decide how to handle the session locking cases

        if (isSessionMissing.getAndSet(false)) {
            mLatch = new CountDownLatch(1);
            fetchSession(request);
            mLatch.countDown();
        }

        if (!isSessionEndpoint(request)) {
            try {
                mLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void fetchSession(Request request) throws IOException {
        log(request, "fetching session");
        Request sessionRequest = new Request.Builder().url(sessionEndpoint).build();
        Call call = ShopGun.getInstance().getClient().newCall(sessionRequest);
        Response response = call.execute();
        response.body().close();
        log(request, "Session update complete");
    }

    private void log(Request request, String msg) {
        SgnLog.d(TAG, "[" + request.toString() + "] " + msg);
    }

}
