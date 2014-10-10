package com.eTilbudsavis.etasdk.pageflip;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class PageflipSinglePage extends PageflipPage {

	public static PageflipPage newInstance(Catalog c, int position) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, position );
		PageflipPage f = new PageflipSinglePage();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void loadPages() {
		ImageLoader.getInstance().displayImage(new ImageRequest(getPageLeft().getThumb(), getPhotoView()));
	}
	
}
