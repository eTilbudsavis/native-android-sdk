package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.Map;

public class NetworkResponse {

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /** Response headers. */
    public final Map<String, String> headers;
    
    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache
     */
    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers ) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
    }
    
}
