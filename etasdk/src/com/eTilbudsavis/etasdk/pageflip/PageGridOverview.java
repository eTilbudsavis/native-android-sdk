package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.ImageLoader;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.utils.Utils;

@SuppressWarnings("deprecation")
public class PageGridOverview extends DialogFragment {
	
	public static final String TAG = Eta.TAG_PREFIX + PageGridOverview.class.getSimpleName();
	
	private static final String ARG_CATALOG = Eta.ARG_PREFIX + "pageGridOverview.catalog";
	private static final String ARG_PAGE = Eta.ARG_PREFIX + "pageGridOverview.page";
	
	Catalog mCatalog;
	int mPage = 1;
	GridView mGrid;
	OnItemClickListener mListener;
	
	public static PageGridOverview newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageGridOverview f = new PageGridOverview();
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
	
	public void setOnItemClickListener(OnItemClickListener l) {
		mListener = l;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		setUpGrid();
		AlertDialog.Builder b = new Builder(getActivity());
		b.setView(mGrid);
		return b.create();
	}
	
	private static final int MAX_WIDTH = 177;
	private static final int MAX_HEIGHT = 212;
	private static final int MAX_COLUMNS = 3;
	
	private void setUpGrid() {
		
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
	
	public class GalleryAdapter extends BaseAdapter implements SectionIndexer {
		
		
		String[] sections;
		int count = 0;
		Activity a;
		
		public GalleryAdapter() {
			
			a = getActivity();
			count = mCatalog.getPageCount();
			
			sections = new String[count];
			for (int i = 0 ; i < count ; i++ ) {
				sections[i] = String.valueOf(i+1);
			}
			
		}
		
		public int getCount() {
			return count;
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
			
			tv.setBackgroundColor(Color.parseColor("#a0000000"));
			fl.addView(tv);
			
			String url = mCatalog.getPages().get(position).getThumb();
			ImageLoader.getInstance().displayImage(new ImageRequest(url, iv));
			
			return fl;
		}

		public Object[] getSections() {
			// TODO Auto-generated method stub
			return sections;
		}

		public int getPositionForSection(int sectionIndex) {
			// TODO Auto-generated method stub
			return sectionIndex;
		}

		public int getSectionForPosition(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
	}
}
