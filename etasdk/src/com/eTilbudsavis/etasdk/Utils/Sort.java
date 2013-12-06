package com.eTilbudsavis.etasdk.Utils;

import com.eTilbudsavis.etasdk.Network.Request;

/**
 * Sort is being depricated, for new/updated values, goto {@link Request.Sort Request.Sort}
 * 
 * {@link com.eTilbudsavis.etasdk.Util.Sort Sort} holds all variables describing sorting methods that the server allows.<br><br>
 * 
 * This class should be referenced, when using the 
 * {@link com.eTilbudsavis.etasdk.Api #setOrderBy(String[]) Api.setOrderBy()}
 * to avoid invalid parameters.<br><br>
 * 
 * Sort is referenced in the classes who's endpoints allow the individual sort types. 
 * This minimizes confusion about what sort typen may be used for which lists.
 * For example a catalog list may be sorted by distance, so {@link com.eTilbudsavis.etasdk.EtaObjects.Catalog Catalog}
 * has a {@link com.eTilbudsavis.etasdk.EtaObjects.Catalog #SORT_DISTANCE Catalog.SORT_DISTANCE}
 * <br><br>
 * 
 * NOT ALL sort options is defined in the SDK, please reference the online documentation for more options.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
@Deprecated
public final class Sort {

	/** String identifying the order by parameter for all list calls to the API */
	public static final String ORDER_BY = Request.Param.ORDER_BY;

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
