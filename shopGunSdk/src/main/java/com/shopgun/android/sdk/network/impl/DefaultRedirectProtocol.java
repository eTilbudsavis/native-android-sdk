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

import com.shopgun.android.sdk.network.RedirectProtocol;
import com.shopgun.android.sdk.network.Request;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultRedirectProtocol implements RedirectProtocol {

    private String getFirstHeader(Map<String, List<String>> headers, String header) {
        if (headers.containsKey(header)) {
            return headers.get(header).get(0);
        }
        return null;
    }

    @Override
    public boolean isRedirectRequested(Request<?> request, HttpResponse response, ArrayList<URL> previouslyVisited) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpURLConnection.HTTP_MOVED_PERM
                || statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                || statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
            request.addEvent("request-redirected-statuscode-" + statusCode);
            return true;
        }
        return false;
    }

    @Override
    public URL getRedirectLocation(Request<?> request, HttpResponse response, ArrayList<URL> previouslyVisited) throws IOException {

        URL url = previouslyVisited.get(previouslyVisited.size()-1);

        Header locationHeader = response.getFirstHeader("Location");
        String location = null;
        if (locationHeader != null) {
            location = locationHeader.getValue();
            // Some url's might be missing the host, so we'll add it
            if (!location.startsWith("http")) {
                location = url.getProtocol() + "://" + url.getHost() + location;
            }
        }

        if (location == null) {
            request.addEvent("no-location-header-identified");
        } else {
            URL redirectUrl = new URL(location);
            if (isInfiniteLoop(previouslyVisited, redirectUrl)) {
                request.addEvent("infinite-number-of-redirects-detected");
                return null;
            } else {
                request.addEvent("redirecting-request: " + location);
                return redirectUrl;
            }
        }
        return null;

    }

    @Override
    public void onRedirectComplete(Request<?> request, ArrayList<URL> previouslyVisited) throws IOException {

    }

    /**
     * Inefficient, and simple check, but usually we won't have too many redirects so it shouldn't matter.
     * @param previouslyVisited A {@link ArrayList} to check for recursion
     * @return {@code true} if two identical url's are detected, else {@code false}
     */
    protected boolean isInfiniteLoop(ArrayList<URL> previouslyVisited, URL redirectUrl) {
        for (int i = 0; i < previouslyVisited.size(); i++) {
            URL outer = previouslyVisited.get(i);
            for (int j = i+1; j < previouslyVisited.size(); j++) {
                URL inner = previouslyVisited.get(j);
                if (outer.equals(inner)) {
                    return true;
                }
            }
            if (outer.equals(redirectUrl)) {
                return true;
            }
        }
        return false;
    }

}
