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

    /**
     * Determine if a request should be redirected to a new location, given the {@link HttpResponse} from the server
     * @param request A {@link Request}
     * @param response The {@link HttpResponse response} from the server, including statusCode and headers. But no {@link org.apache.http.HttpEntity HttpEntity}.
     * @param previouslyVisited All previously visited links in this redirect chain
     * @return {@code true} if the network should redirect, else {@code false}
     * @throws IOException
     */
    boolean isRedirectRequested(Request<?> request, HttpResponse response, ArrayList<URL> previouslyVisited) throws IOException;

    /**
     * Determine the new location the request is expected to be redirected to given the {@link HttpResponse} from the server
     * and the current execution context.
     * @param request A {@link Request}
     * @param response The {@link HttpResponse response} from the server, including statusCode and headers. But no {@link org.apache.http.HttpEntity HttpEntity}.
     * @param previouslyVisited All previously visited links in this redirect chain
     * @return A redirect {@link URL}
     * @throws IOException
     */
    URL getRedirectLocation(Request<?> request, HttpResponse response, ArrayList<URL> previouslyVisited) throws IOException;

    /**
     * When the final {@link URL} in a chain of redirects have been resolved, this method is called. In some instances
     * the etilbudsavis.dk APi will return the content of a request in the redirect url, rather than in the body, this
     * would be a great time to resolve those.
     *
     * <p>To avoid network overhead of downloading an irrelevant body content, this method can optionally throw an exception.</p>
     * @param request A {@link Request}
     * @param urls All previously visited links in this redirect chain
     * @throws IOException to avoid reading the body of the response.
     */
    void onRedirectComplete(Request<?> request, ArrayList<URL> urls) throws IOException;

}
