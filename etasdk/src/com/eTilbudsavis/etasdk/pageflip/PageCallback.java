package com.eTilbudsavis.etasdk.pageflip;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public interface PageCallback {

	/**
	 * Get the catalog bring displayed
	 * @return A catalog
	 */
	public Catalog getCatalog();
	
	/**
	 * The current configuration of the device
	 * @return true if the device is in landscape, else false
	 */
	public boolean isLandscape();
	
	/**
	 * Determining, if the {@link PageflipViewPager} if in the same position, as the position in the
	 * {@link PageflipFragment}. Is among other things used to determine, if the {@link PageflipFragment}
	 * and {@link PageflipViewPager} is ready to display the catalog pages.
	 * @return
	 */
	public boolean isPositionSet();
	
	/**
	 * Check if the device has small heap size
	 * @return true if heap size is small.
	 */
	public boolean isLowMemory();
	
	/**
	 * Get the {@link PageflipListener} that the {@link PageflipFragment} contains, in order to print debug info.
	 * @return A {@link PageflipListener}
	 */
	public PageflipListener getWrapperListener();
	
	/**
	 * Get the current view-session. A variable used to give correct statistics to the eTilbudsavis API.
	 * @return A string representation of the view-session token
	 */
	public String getViewSession();
	
	/**
	 * Called by a {@link PageflipFragment} when it's ready to display catalog pages. This is used
	 * to control events during configuration changes
	 * @param position The position of the {@link PageFragment}
	 */
	public void onReady(int position);
	
}