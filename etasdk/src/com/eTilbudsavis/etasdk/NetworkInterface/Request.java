package com.eTilbudsavis.etasdk.NetworkInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public abstract class Request<T> implements Comparable<Request<T>> {

	public static final String TAG = "Request";

	/** Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}. */
	protected static final String DEFAULT_PARAMS_ENCODING = "utf-8";

	/** Listener interface, for responses */
	private final Listener<T> mListener;

	/** Request method of this request.  Currently supports GET, POST, PUT, and DELETE. */
	private final int mMethod;

	/** URL of this request. */
	private String mUrl;

	/** Headers to be used in this request */
	private Map<String, String> mHeaders = new HashMap<String, String>();

	/** Parameters to use in this request */
	private Bundle mQuery = new Bundle();

	/** Sequence number used for prioritizing the queue */
	private int mSequence = 0;

	/** Should this request use location in the query */
	private boolean mUseLocation = true;

	/** If true Request will return data from cache if exists */
	private boolean mUseCache = true;

	/** Whether or not responses to this request should be cached. */
	private boolean mShouldCache = true;

	/** Whether to only return cache item. */
	private boolean mReturnCacheOnlyIfPossible = false;

	/** Prints debug information when a request is done */
	private boolean mPrintDebug = false;

	/** Whether or not this request has been canceled. */
	private boolean mCanceled = false;

	/** Determine if we should try to update session */
	private boolean mSessionUpdate = true;
	
	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	/** Supported request methods. */
	public interface Method {
		int GET = 0;
		int POST = 1;
		int PUT = 2;
		int DELETE = 3;
	}

	/**
	 * Creates a new request with the given method (one of the values from {@link Method}),
	 * URL, and error listener.  Note that the normal response listener is not provided here as
	 * delivery of responses is provided by subclasses, who have a better idea of how to deliver
	 * an already-parsed response.
	 */
	public Request(int method, String url, Listener<T> listener) {
		mMethod = method;
		mUrl = url;
		mListener = listener;
	}

	/** Mark this request as canceled.  No callback will be delivered. */
	public void cancel() {
		mCanceled = true;
	}

	/** Returns true if this request has been canceled. */
	public boolean isCanceled() {
		return mCanceled;
	}

	/** Returns a list of headers for this request. */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}

	public void setHeaders(Map<String, String> headers) {
		mHeaders.putAll(headers);
	}

	/**
	 * Return the method for this request.  Can be one of the values in {@link Method}.
	 */
	public int getMethod() {
		return mMethod;
	}

	/**
	 * Returns the response listener for this request
	 * @return
	 */
	public Listener getListener() {
		return mListener;
	}

	/**
	 * returns wether this request is cachable or not
	 * @return
	 */
	public boolean shouldCache() {
		return mShouldCache;
	}

	protected void shouldCache(boolean cacheable) {
		mShouldCache = cacheable;
	}
	
	/**
	 * Method for determining if this request should attempt a session update
	 * @return true if session update should be attempted
	 */
	public boolean doSessionUpdate() {
		return mSessionUpdate;
	}

	/**
	 * Method for enabeling or disabeling session updates, on errors.
	 */
	public void doSessionUpdate(boolean update) {
		mSessionUpdate = update;
	}
	
	/**
	 * Returns true, if this request should return cache, else false
	 * @return
	 */
	public boolean useCache() {
		return mUseCache;
	}

	/**
	 * Set whether this request may use data from cache or not
	 * @param useCache
	 * @return
	 */
	public Request useCache(boolean useCache) {
		mUseCache = useCache;
		return Request.this;
	}

	/**
	 * If true, this request wants debug information about the http request
	 * printed to consol
	 */
	public boolean printDebug() {
		return mPrintDebug;
	}

	public Request printDebug(boolean print) {
		mPrintDebug = print;
		return Request.this;
	}

	public boolean useLocation() {
		return mUseLocation;
	}

	public Request useLocation(boolean useLocation) {
		mUseLocation = useLocation;
		return Request.this;
	}

	public void cacheOnlyIfPossible(boolean onlyCache) {
		mReturnCacheOnlyIfPossible = onlyCache;
	}

	public boolean cacheOnlyIfPossible() {
		return mReturnCacheOnlyIfPossible;
	}

	/** Return the url for this request. */
	public String getUrl() {
		return mUrl;
	}

	/** Set the url of this request. */
	public Request setUrl(String url) {
		mUrl = url;
		return Request.this;
	}

	protected Bundle getQueryParameters() {
		return mQuery;
	}

	public Request putQueryParameters(Bundle query) {
		mQuery.putAll(query);
		return Request.this;
	}

	protected Priority getPriority() {
		return Priority.MEDIUM;
	}

	protected int getSequence() {
		return mSequence;
	}

	protected void setSequence(int seq) {
		mSequence = seq;
	}

	protected String getParamsEncoding() {
		return DEFAULT_PARAMS_ENCODING;
	}

	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}
	
	public byte[] getBody() {
		Bundle params = getQueryParameters();
		if (params != null && params.size() > 0) {
			try {
				return Utils.bundleToQueryString(params).getBytes(getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	abstract protected Response<T> parseNetworkResponse(NetworkResponse response);
	
	abstract protected void deliverResponse(T response);

	public int compareTo(Request<T> other) {
		Priority left = this.getPriority();
		Priority right = other.getPriority();
		return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
	}
	
    public boolean mayCache(NetworkResponse response) {
    	String cacheControl = "Cache-Control";
    	String noCache = "no-cache";
    	String noStore = "no-store";
    	return false;
    }
    
	
	public void printDebug(Request request, NetworkResponse response) {

		String newLine = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
		sb.append("*** Pre execute ***").append(newLine);
		sb.append("Method: ").append(getMethod()).append(newLine);
		sb.append("Url: ").append(getUrl()).append(newLine);
		sb.append("Body: ").append(getUrl()).append(newLine);
		sb.append("Headers: ").append(getHeaders().toString()).append(newLine);
		sb.append("*** Post Execute***").append(newLine);
		sb.append("StatusCode: ").append(response.statusCode).append(newLine);
		sb.append("Headers: ");
		for (String k : response.headers.keySet()) {
			sb.append(k).append(": ").append(response.headers.get(k)).append(", ");
		}
		String data = null;
		try {
			data = new String(response.data, getParamsEncoding());
		} catch (Exception e) {
			data = "not of type string";
		}
		sb.append("Object: ").append(data.length() > 100 ? data.substring(0, 100) : data);
		Utils.logd(TAG, sb.toString());
	}

	/**
	 * Helper class for the parameters the eTilbudsavis API supports.<br>
	 * Note that not all parameters are necessarily in this set.<br><br>
	 * 
	 * For more information on parameters, please read the API documentation at engineering.etilbudsavis.dk/eta-api/
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Param {

		/** String identifying the order by parameter for all list calls to the API */
		public static final String ORDER_BY = "order_by";

		/** API v2 parameter name for sensor. */
		public static final String SENSOR = "r_sensor";

		/** API v2 parameter name for latitude. */
		public static final String LATITUDE = "r_lat";

		/** API v2 parameter name for longitude. */
		public static final String LONGITUDE = "r_lng";

		/** API v2 parameter name for radius. */
		public static final String RADIUS = "r_radius";

		/** API v2 parameter name for bounds east. */
		public static final String BOUND_EAST = "b_east";

		/** API v2 parameter name for bounds north. */
		public static final String BOUND_NORTH = "b_north";

		/** API v2 parameter name for bounds south. */
		public static final String BOUND_SOUTH = "b_south";

		/** API v2 parameter name for bounds west. */
		public static final String BOUND_WEST = "b_west";

		/** API v2 parameter name for API Key */
		public static final String API_KEY = "api_key";

		/** String identifying the offset parameter for all list calls to the API */
		public static final String OFFSET = "offset";

		/** String identifying the limit parameter for all list calls to the API */
		public static final String LIMIT = "limit";

		/** String identifying the run from parameter for all list calls to the API */
		public static final String RUN_FROM = "run_from";

		/** String identifying the run till parameter for all list calls to the API */
		public static final String RUN_TILL = "run_till";

		/** String identifying the color parameter for all list calls to the API */
		public static final String COLOR = "color";

		/** Parameter for pdf file location */
		public static final String PDF = "pdf";

		/** Parameter for a resource name, e.g. dealer name */
		public static final String NAME = "name";

		/** Parameter for a dealer resource */
		public static final String DEALER = "dealer";

		/** Parameter for the friendly name of a website */
		public static final String URL_NAME = "url_name";

		/** Parameter for pageflip color */
		public static final String PAGEFLIP_COLOR = "pageflip_color";

		/** Parameter for the absolute address of a website */
		public static final String WEBSITE = "website";

		/** Parameter for a resource logo */
		public static final String LOGO = "logo";

		/** Parameter for search */
		public static final String QUERY = "query";

		/** Parameter for pageflip logo location */
		public static final String PAGEFLIP_LOGO = "pageflip_Logo";

		/** Parameter for catalog id's */
		public static final String FILTER_CATALOG_IDS = "catalog_ids";

		/** Parameter for store id's */
		public static final String FILTER_STORE_IDS = "store_ids";

		/** Parameter for area id's */
		public static final String FILTER_AREA_IDS = "area_ids";

		/** Parameter for store id's */
		public static final String FILTER_OFFER_IDS = "offer_ids";

		/** Parameter for getting a list of specific dealer id's */
		public static final String FILTER_DEALER_IDS = "dealer_ids";

		/** Parameter for a resource e-mail */
		public static final String EMAIL = "email";

		/** Parameter for a resource password */
		public static final String PASSWORD = "password";

		/** Parameter for a resource birth year */
		public static final String BIRTH_YEAR = "birth_year";

		/** Parameter for a resource gender */
		public static final String GENDER = "gender";

		/** Parameter for a resource success redirect */
		public static final String SUCCESS_REDIRECT = "success_redirect";

		/** Parameter for a resource error redirect */
		public static final String ERROR_REDIRECT = "error_redirect";

		/** Parameter for a resource old password */
		public static final String OLD_PASSWORD = "old_password";

		/** Parameter for a facebook token */
		public static final String FACEBOOK_TOKEN = "facebook_token";

		/** Parameter for a delete filter */
		public static final String FILTER_DELETE = "filter";

		public static final String ID = "id";
		public static final String MODIFIED = "modified";
		public static final String ERN = "ern";
		public static final String ACCESS = "access";
		public static final String ACCEPT_URL = "accept_url";

		public static final String DESCRIPTION = "description";
		public static final String COUNT = "count";
		public static final String TICK = "tick";
		public static final String OFFER_ID = "offer_id";
		public static final String CREATOR = "creator";
		public static final String SHOPPING_LIST_ID = "shopping_list_id";

	}

	/**
	 * Helper class for headers the eTilbudsavis API uses
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Header {

		/** Header name for the session token */
		public static final String X_TOKEN = "X-Token";

		/** Header name for the session expire token */
		public static final String X_TOKEN_EXPIRES = "X-Token-Expires";

		/** Header name for the signature */
		public static final String X_SIGNATURE = "X-Signature";

		/** Header name for content_type */
		public static final String CONTENT_TYPE = "Content-Type";

	}

	/**
	 * Helper class for the sort orders the eTilbudsavis API supports.<br>
	 * These are typically used for requests to any list endpoint.<br>
	 * Note that not all parameters are necessarily in this set.<br><br>
	 * 
	 * For more information on parameters, please read the API documentation at engineering.etilbudsavis.dk/eta-api/
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Sort {

		/** String identifying the order by parameter for all list calls to the API */
		public static final String ORDER_BY = "order_by";

		/** String identifying the descending variable */
		public static final String DESC = "-";

		/** Sort a list by popularity in ascending order. (smallest to largest) */
		public static final String POPULARITY = "popularity";

		/** Sort a list by distance in ascending order. (smallest to largest) */
		public static final String DISTANCE = "distance";

		/** Sort a list by name in ascending order. (a-z) */
		public static final String NAME = "name";

		/** Sort a list by published in ascending order. (smallest to largest) */
		public static final String PUBLISHED = "published";

		/** Sort a list by expired in ascending order. (smallest to largest) */
		public static final String EXPIRED = "expired";

		/** Sort a list by created in ascending order. (smallest to largest) */
		public static final String CREATED = "created";

		/** Sort a list by page (in catalog) in ascending order. (smallest to largest) */
		public static final String PAGE = "page";

		/** Sort a list by popularity in descending order. (largest to smallest)*/
		public static final String POPULARITY_DESC = DESC + POPULARITY;

		/** Sort a list by distance in descending order. (largest to smallest)*/
		public static final String DISTANCE_DESC = DESC + DISTANCE;

		/** Sort a list by name in descending order. (z-a)*/
		public static final String NAME_DESC = DESC + NAME;

		/** Sort a list by published in descending order. (largest to smallest)*/
		public static final String PUBLISHED_DESC = DESC + PUBLISHED;

		/** Sort a list by expired in descending order. (largest to smallest)*/
		public static final String EXPIRED_DESC = DESC + EXPIRED;

		/** Sort a list by created in ascending order. (smallest to largest) */
		public static final String CREATED_DESC = DESC + CREATED;

		/** Sort a list by page (in catalog) in descending order. (largest to smallest)*/
		public static final String PAGE_DESC = DESC + PAGE;

	}

	/**
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 */
	public static class Endpoint {
		

		// GLOBALS
		public static final String PRODUCTION = "https://api.etilbudsavis.dk";
		public static final String EDGE = "https://edge.etilbudsavis.dk";
		public static final String STAGING = "https://staging.etilbudsavis.dk";

		// LISTS
		public static final String CATALOG_LIST = "/v2/catalogs";
		public static final String CATALOG_ID = "/v2/catalogs/";
		public static final String CATALOG_SEARCH = "/v2/catalogs/search";
		
		public static final String DEALER_LIST = "/v2/dealers";
		public static final String DEALER_ID = "/v2/dealers/";
		public static final String DEALER_SEARCH = "/v2/dealers/search";
		
		public static final String OFFER_LIST = "/v2/offers";
		public static final String OFFER_ID = "/v2/offers/";
		public static final String OFFER_SEARCH = "/v2/offers/search";
		public static final String OFFER_TYPEAHEAD = "/v2/offers/typeahead";
		
		public static final String STORE_LIST = "/v2/stores";
		public static final String STORE_ID = "/v2/stores/";
		public static final String STORE_SEARCH = "/v2/stores/search";
		public static final String STORE_QUICK_SEARCH = "/v2/stores/quicksearch";
		
		public static final String SESSIONS = "/v2/sessions";
		
		public static final String USER_ID = "/v2/users/";
		public static final String USER_RESET = "/v2/users/reset";
		
		public static final String CATEGORIES	= "/v2/categories";

		/**
		 * https://etilbudsavis.dk/proxy/{id}/
		 */
		public static String getPageflipProxy(String id) {
			return String.format("http://etilbudsavis.dk/proxy/%s/", id);
		}

		/**
		 * /v2/users/{user_id}/facebook
		 */
		public static String getFacebook(int userId) {
			return String.format("/v2/users/%s/facebook", userId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists
		 */
		public static String getLists(int userId) {
			return String.format("/v2/users/%s/shoppinglists", userId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}
		 * @param userId
		 * @param listId
		 * @return
		 */
		public static String getList(int userId, String listId) {
			return String.format("/v2/users/%s/shoppinglists/%s", userId, listId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/modified
		 */
		public static String getListModified(int userId, String listId) {
			return String.format("/v2/users/%s/shoppinglists/%s/modified", userId, listId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/empty
		 */
		public static String getListEmpty(int userId, String listId) {
			return String.format("/v2/users/%s/shoppinglists/%s/empty", userId, listId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares
		 * @param userId
		 * @param listId
		 * @return
		 */
		public static String getListShares(int userId, String listId) {
			return String.format("/v2/users/%s/shoppinglists/%s/shares", userId, listId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/shares/{email}
		 */
		public static String getListShareEmail(int userId, String listId, String email) {
			return String.format("/v2/users/%s/shoppinglists/%s/shares/%s", userId, listId, email);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items
		 */
		public static String getItems(int userId, String listId) {
			return String.format("/v2/users/%s/shoppinglists/%s/items", userId, listId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}
		 */
		public static String getItem(int userId, String listId, String itemId) {
			return String.format("/v2/users/%s/shoppinglists/%s/items/%s", userId, listId, itemId);
		}

		/**
		 * /v2/users/{user_id}/shoppinglists/{list_uuid}/items/{item_uuid}/modified
		 */
		public static String getItemModifiedById(int userId, String listId, String itemId) {
			return String.format("/v2/users/%s/shoppinglists/%s/items/%s/modified", userId, listId, itemId);
		}

	}
}
