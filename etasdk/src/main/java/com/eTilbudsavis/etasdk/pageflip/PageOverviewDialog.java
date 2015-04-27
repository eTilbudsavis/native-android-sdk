/*******************************************************************************
 * Copyright 2015 eTilbudsavis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.imageloader.impl.FadeBitmapDisplayer;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;
import com.eTilbudsavis.etasdk.utils.ColorUtils;
import com.eTilbudsavis.etasdk.utils.Utils;

@SuppressWarnings("deprecation")
public class PageOverviewDialog extends DialogFragment {
	
	public static final String TAG = Constants.getTag(PageOverviewDialog.class);

	private static final String ARG_CATALOG = Constants.getArg("pageGridOverview.catalog");
	private static final String ARG_PAGE = Constants.getArg("pageGridOverview.page");

	private static final int MAX_WIDTH = 177;
//	private static final int MAX_HEIGHT = 212;
	private static final int MAX_COLUMNS = 3;
	
	private Catalog mCatalog;
	private int mPage = 1;
	private GridView mGrid;
	private OnItemClickListener mListener;
	
	public static PageOverviewDialog newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageOverviewDialog f = new PageOverviewDialog();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getArguments()!=null) {
			setCatalog((Catalog) getArguments().getSerializable(ARG_CATALOG));
			setPage(getArguments().getInt(ARG_PAGE));
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (getDialog() != null) {
			getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		}
		
		int spacePx = Utils.convertDpToPx(6, getActivity());
		
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mGrid = new GridView(getActivity());
		mGrid.setLayoutParams(lp);
		mGrid.setPadding(spacePx, spacePx, spacePx, spacePx);
		mGrid.setAdapter(new GalleryAdapter());
		mGrid.setFastScrollEnabled(true);
		if (android.os.Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			mGrid.setFastScrollAlwaysVisible(true);
		}
		
		mGrid.setHorizontalSpacing(spacePx);
		mGrid.setVerticalSpacing(spacePx);
//		mGrid.setColumnWidth(columnWidth);
		mGrid.setGravity(Gravity.CENTER_HORIZONTAL);
		mGrid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		mGrid.setBackgroundColor(mCatalog.getBranding().getColor());
		
		Point screen = PageflipUtils.getDisplayDimen(getActivity());
		int columns = (int)Math.floor(screen.x/MAX_WIDTH);
		mGrid.setNumColumns(Math.min(columns, MAX_COLUMNS));

		mGrid.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismiss();
				if (mListener!=null) {
					int page = position+1;
					mListener.onItemClick(parent, view, page, id);
				}
			}
		});
		
		if (mPage != 1) {
			mGrid.postDelayed(new Runnable() {
				
				public void run() {
					if (isAdded()) {
						mGrid.setSelection(mPage-1);
					}
				}
			}, 300);
		}
		
		return mGrid;
	}

	public void setOnItemClickListener(OnItemClickListener l) {
		mListener = l;
	}
	
	public void setCatalog(Catalog c) {
		mCatalog = c;
	}
	
	public void setPage(int page) {
		mPage = page;
	}
	
	@Override
	public void dismiss() {
		mGrid.setAdapter(null);
		super.dismiss();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mCatalog.getPages() == null || mCatalog.getPages().isEmpty()) {
			dismiss();
		}
	}
	
	public class GalleryAdapter extends BaseAdapter implements SectionIndexer {
		
		
		String[] sections;
		Activity a;
		
		public GalleryAdapter() {
			
			a = getActivity();
			
			sections = new String[getCount()];
			for (int i = 0 ; i < getCount() ; i++ ) {
				sections[i] = String.valueOf(i+1);
			}
			
		}
		
		public int getCount() {
			return mCatalog.getPages() == null ? 0 : mCatalog.getPages().size();
		}

		public Object getItem(int position) {
			return mCatalog.getPages().get(position).getThumb();
		}

		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			int wrap = LayoutParams.WRAP_CONTENT;
			int fill = LayoutParams.FILL_PARENT;
			
			GridView.LayoutParams lp = new GridView.LayoutParams(fill, fill);
			FrameLayout fl = new FrameLayout(a);
			fl.setLayoutParams(lp);
			
			int ivSize = Utils.convertDpToPx(115, a);
			FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(ivSize, ivSize, Gravity.CENTER_HORIZONTAL);
			ImageView iv = new ImageView(a);
			iv.setLayoutParams(ivlp);
			fl.addView(iv);
			
			int g = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
			FrameLayout.LayoutParams tvlp = new FrameLayout.LayoutParams(wrap, wrap, g);
			TextView tv = new TextView(a);
			tv.setText(String.valueOf(position+1));
			tv.setTextColor(Color.WHITE);
			tv.setTextSize(20);
			tv.setTypeface(null, Typeface.BOLD);
			tv.setLayoutParams(tvlp);
			
			int vpad = Utils.convertDpToPx(0, a);
			int hpad = Utils.convertDpToPx(5, a);
			tv.setPadding(hpad, vpad, hpad, vpad);
			
			tv.setBackgroundDrawable(getDrawable());
			fl.addView(tv);
			
			String url = mCatalog.getPages().get(position).getThumb();
			ImageRequest ir = new ImageRequest(url, iv);
			ir.setBitmapDisplayer(new FadeBitmapDisplayer(100, false, true, true));
			Eta.getInstance().getImageloader().displayImage(ir);
			
			return fl;
		}
		
		private GradientDrawable getDrawable() {
			int color = mCatalog.getBranding().getColor();
			color = ColorUtils.applyAlpha(color, 160);
			
			float radius = Utils.convertDpToPx(3, getActivity());
			
			GradientDrawable gd = new GradientDrawable();
			gd.setColor(color);
			gd.setCornerRadius(radius);
			gd.setStroke(1, 0x80303030);
			return gd;
		}
		
		public Object[] getSections() {
			return sections;
		}

		public int getPositionForSection(int sectionIndex) {
			return sectionIndex;
		}

		public int getSectionForPosition(int position) {
			return position;
		}
		
	}
}
