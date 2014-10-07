package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.R.id;
import com.eTilbudsavis.etasdk.R.layout;
import com.eTilbudsavis.etasdk.photoview.PhotoView;

public class DoublePage extends PageflipPage {

	private static final String CATALOG = "catalog";
	private static final String PAGE = "page_num";
	private static final String ZOOM = "zoom";
	
	private Catalog mCatalog;
	private PhotoView mLeftPage;
	private PhotoView mRightPage;
	private int mPageNum = 0;
	private Page mPage;
	
	public static DoublePage newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, page);
		DoublePage f = new DoublePage();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments()!=null) {
			
			mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
			mPageNum = getArguments().getInt(PAGE);
			mPage = mCatalog.getPages().get(mPageNum-1);
			
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		outState.putFloat(ZOOM, mPhotoView.getScale());
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
	}
	
	private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.singlepage, viewGroup);
        
        mLeftPage = (PhotoView) v.findViewById(R.id.etasdk_pageflip_left_page);
        mRightPage = (PhotoView) v.findViewById(R.id.etasdk_pageflip_right_page);
        
        // Find your buttons in subview, set up onclicks, set up callbacks to your parent fragment or activity here.
        
        // You can create ViewHolder or separate method for that.
        // example of accessing views: TextView textViewExample = (TextView) view.findViewById(R.id.text_view_example);
        // textViewExample.setText("example");
    }
}
