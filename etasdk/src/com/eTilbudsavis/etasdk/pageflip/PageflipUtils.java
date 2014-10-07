package com.eTilbudsavis.etasdk.pageflip;

public class PageflipUtils {
	
	private PageflipUtils() {
		// TODO Auto-generated constructor stub
	}
	

	public static int pageToPosition(int page) {
		if (page <= 1) {
			return 1;
		}
		return ( (page-(page%2)) /2)+1;
	}
	
	public static int positionToPage(int position) {
		if (position <= 0) {
			return 1;
		}
		return (position-1)*2;
	}
	
	
}
