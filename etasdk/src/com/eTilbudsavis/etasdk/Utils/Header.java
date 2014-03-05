package com.eTilbudsavis.etasdk.Utils;

/**
 * Helper class for headers the eTilbudsavis API uses
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class Header {
	
	/** Header name for the session token */
	public static final String X_TOKEN = "X-Token";

	/** Header name for the session expire token */
	public static final String X_TOKEN_EXPIRES = "X-Token-Expires";

	/** Header name for the signature */
	public static final String X_SIGNATURE = "X-Signature";

	/** Header name for content_type */
	public static final String CONTENT_TYPE = "Content-Type";

	/** Header name for content_type */
	public static final String RETRY_AFTER = "Retry-After";

	/** Header name for cash control */
	public static final String CACHE_CONTROL = "Cache-Control";
	
	class Values {
		public static final String NO_CACHE = "no-cache";
		public static final String NO_STORE = "no-store";
	}
	
}
