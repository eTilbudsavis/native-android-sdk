package com.eTilbudsavis.etasdk.ImageLoader;

/**
 * Interface for displaying a bitmap in an ImageView
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface BitmapDisplayer {
	
	/**
	 * A method for displaying the Bitmap that have been downloaded/gotten from cache
	 * @param ir A ImageRequest holding the Bitmap to display and the ImageView
	 *           to display it in. 
	 */
	public void display(ImageRequest ir);
	
}
