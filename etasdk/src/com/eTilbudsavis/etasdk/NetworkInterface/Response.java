package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.Map;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onComplete(T response, EtaError error);
    }
    
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
    
    public boolean isSuccess() {
    	return error == null;
    }
    
}
