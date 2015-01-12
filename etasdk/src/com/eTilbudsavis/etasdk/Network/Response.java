/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network;

import java.util.Map;

public class Response<T> {
	
    /** Parsed response, or null in the case of error. */
    public final T result;
    
    /** Detailed error information if <code>errorCode != OK</code>. */
    public final EtaError error;

	/** Item for containing cache items */
	public Map<String, Cache.Item> cache;
	
    private Response(T result, Map<String, Cache.Item> cache, EtaError error) {
        this.result = result;
        this.error = error;
        this.cache = cache;
    }
    
    /** Returns a successful response containing the parsed result. */
    public static <T> Response<T> fromSuccess(T result, Map<String, Cache.Item> cache) {
        return new Response<T>(result, cache, null);
    }
    
    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> fromError(EtaError error) {
        return new Response<T>(null, null, error);
    }
    
    /**
     * Check is this response is a success
     * @return true if Request succeeded, else false.
     */
    public boolean isSuccess() {
    	return error == null;
    }

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onComplete(T response, EtaError error);
    }
    
}
