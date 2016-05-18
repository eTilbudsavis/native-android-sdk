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

package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.network.HttpStack;
import com.shopgun.android.sdk.network.RedirectProtocol;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.utils.HeaderUtils;
import com.shopgun.android.sdk.utils.Utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class HttpURLNetwork implements HttpStack {

    public static final String TAG = Constants.getTag(HttpURLNetwork.class);

    Pattern mHostPatternPrefix = Pattern.compile("^(shopgun\\.com|etilbudsavis\\.dk|api\\.etilbudsavis\\.dk|api-edge\\.etilbudsavis\\.dk|api-staging\\.etilbudsavis\\.dk).*$");
    Pattern mHostPatternPostfix = Pattern.compile(".*?(shopgun\\.com|etilbudsavis\\.dk)");

    final RedirectProtocol mRedirectProtocol;

    public HttpURLNetwork(RedirectProtocol redirectProtocol) {
        mRedirectProtocol = redirectProtocol;
    }

    public HttpResponse performNetworking(Request<?> request) throws IOException {
        String tmpUrl = Utils.requestToUrlAndQueryString(request);
        ArrayList<URL> urls = new ArrayList<URL>();
        urls.add(new URL(tmpUrl));
        return performNetworking(request, urls);
    }

    private HttpResponse performNetworking(Request<?> request, ArrayList<URL> urls) throws IOException {

        URL url = urls.get(urls.size()-1);
        HttpURLConnection connection = openConnection(request, url);
        HttpResponse response = getHttpResponse(connection);

        if (response.getStatusLine().getStatusCode() == -1) {
            throw new IOException("Connection returned invalid response code.");
        }

        if (mRedirectProtocol.isRedirectRequested(request, response, urls)) {
            URL redirectUrl = mRedirectProtocol.getRedirectLocation(request, response, urls);
            if (redirectUrl != null) {
                urls.add(redirectUrl);
                return performNetworking(request, urls);
            }
        }

        HttpEntity entity = getEntity(connection);
        response.setEntity(entity);

        if (urls.size() > 1) {
            mRedirectProtocol.onRedirectComplete(request, urls);
        }

        return response;
    }

    private HttpURLConnection openConnection(Request<?> request, URL url) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(request.getTimeOut());
        connection.setReadTimeout(request.getTimeOut());
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);

        setHeaders(request, connection);
        setRequestMethod(connection, request);
        return connection;
    }

    private void setHeaders(Request<?> request, HttpURLConnection connection) {
        HashMap<String, String> headers = new HashMap<String, String>(request.getHeaders().size());
        headers.putAll(request.getHeaders());
        for (String key : headers.keySet())
            connection.setRequestProperty(key, headers.get(key));
    }

    private void setRequestMethod(HttpURLConnection connection, Request<?> request) throws IOException {

        String method = request.getMethod().toString();
        connection.setRequestMethod(method);

        switch (request.getMethod()) {
            case POST:
            case PUT:
                addBodyIfExists(connection, request);
                break;
            default:
                break;

        }
    }

    private HttpResponse getHttpResponse(HttpURLConnection connection) throws IOException {

        ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
        String rm = connection.getResponseMessage();
        HttpResponse response = new BasicHttpResponse(pv, connection.getResponseCode(), rm);

        for (Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue().get(0));
                response.addHeader(h);
            }
        }

        return response;
    }

    private static HttpEntity getEntity(HttpURLConnection connection) {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    private static void addBodyIfExists(HttpURLConnection connection, Request<?> request) throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HeaderUtils.CONTENT_TYPE, request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }

}
