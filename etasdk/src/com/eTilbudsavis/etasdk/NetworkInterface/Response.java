package com.eTilbudsavis.etasdk.NetworkInterface;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onComplete(boolean isCache, T response, EtaError error);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** Parsed response, or null in the case of error. */
    public final Cache.Item cache;
    
    /** Detailed error information if <code>errorCode != OK</code>. */
    public final EtaError error;

    /** Detailed error information if <code>errorCode != OK</code>. */
    public final boolean isCache;
    
    private Response(T result, Cache.Item cache, EtaError error, boolean isCache) {
        this.result = result;
        this.error = error;
        this.cache = cache;
        this.isCache = isCache;
    }
    
    /** Returns a successful response containing the parsed result. */
    public static <T> Response<T> fromSuccess(T result, Cache.Item cache, boolean isCache) {
        return new Response<T>(result, cache, null, isCache);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> fromError(EtaError error) {
        return new Response<T>(null, null, error, false);
    }
    
    public boolean isSuccess() {
    	return error == null;
    }
    
}
