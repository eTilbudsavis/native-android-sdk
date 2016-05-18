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

package com.shopgun.android.sdk.network;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public interface RedirectProtocol {

    boolean isRedirectRequested(Request<?> request, HttpResponse response, ArrayList<URL> previouslyVisited) throws IOException;

    /**
     * When the final {@link URL} in a chain of redirects have been resolved, this method is called.
     * To avoid network overhead of downloading an irrelevant body content, this method can optionally be
     * {@link Override overridden} and throw an exception.
     * @param request The request being performed
     * @param urls The {@link ArrayList list} of resolved {@link URL}.
     * @throws IOException to avoid reading the body of the response.
     */
    URL getRedirectLocation(Request<?> request, HttpResponse response, ArrayList<URL> urls) throws IOException;

    void onRedirectComplete(Request<?> request, ArrayList<URL> urls) throws IOException;

}
