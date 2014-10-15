package com.eTilbudsavis.etasdk.pageflip;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class PageflipSinglePage extends PageflipPage {
	
	public static PageflipPage newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, page);
		PageflipPage f = new PageflipSinglePage();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void loadPages() {
		ImageRequest ir = new ImageRequest(getPageLeft().getView(), getPhotoView());
		ir.setBitmapDisplayer(new PageflipBitmapDisplayer());
		addRequest(ir);
	}
	
}
