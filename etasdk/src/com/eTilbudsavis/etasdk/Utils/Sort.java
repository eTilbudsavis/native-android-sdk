package com.eTilbudsavis.etasdk.Utils;

/**
 * Helper class for the sort orders the eTilbudsavis API supports.<br>
 * These are typically used for requests to any list endpoint.<br>
 * Note that not all parameters are necessarily in this set.<br><br>
 * 
 * For more information on parameters, please read the API documentation at our 
 * <a href="http://engineering.etilbudsavis.dk/">Engineering Blog</a>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class Sort {

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
	public static final String PUBLICATION_DATE = "publication_date";

	/** Sort a list by expired in ascending order. (smallest to largest) */
	public static final String EXPIRATION_DATE = "expiration_date";

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String CREATED = "created";

	/** Sort a list by page (in catalog) in ascending order. (smallest to largest) */
	public static final String PAGE = "page";

	/** Sort a list by it's internal score in ascending order. (smallest to largest) */
	public static final String SCORE = "score";
	
	/** Sort a list by it's internal score descending order. (largest to smallest) */
	public static final String SCORE_DESC = DESC + SCORE;
	
	/** Sort a list by price in ascending order. (smallest to largest) */
	public static final String PRICE = "price";

	/** Sort a list by popularity in descending order. (largest to smallest)*/
	public static final String POPULARITY_DESC = DESC + POPULARITY;

	/** Sort a list by distance in descending order. (largest to smallest)*/
	public static final String DISTANCE_DESC = DESC + DISTANCE;

	/** Sort a list by name in descending order. (z-a)*/
	public static final String NAME_DESC = DESC + NAME;

	/** Sort a list by published in descending order. (largest to smallest)*/
	public static final String PUBLICATION_DATE_DESC = DESC + PUBLICATION_DATE;

	/** Sort a list by expired in descending order. (largest to smallest)*/
	public static final String EXPIRATION_DATE_DESC = DESC + EXPIRATION_DATE;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String CREATED_DESC = DESC + CREATED;

	/** Sort a list by page (in catalog) in descending order. (largest to smallest)*/
	public static final String PAGE_DESC = DESC + PAGE;

	/** Sort a list by price in descending order. (largest to smallest)*/
	public static final String PRICE_DESC = DESC + PRICE;

}
