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

import android.view.View;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoDoubleClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoLongClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class SinglePageFragment extends PageFragment {
	
	public static final String TAG = Constants.getTag(SinglePageFragment.class);
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				onSingleClick(getFirstNum(), x, y);
			}
		});
		getPhotoView().setOnPhotoDoubleClickListener(new OnPhotoDoubleClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				onDoubleClick(getFirstNum(), x, y);
			}
		});
		getPhotoView().setOnPhotoLongClickListener(new OnPhotoLongClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				onLongClick(getFirstNum(), x, y);
			}
		});
	};
	
	@Override
	public void loadView() {
		getPhotoView().setTag(null);
		int sampleSize = getCallback().isLowMemory() ? 2 : 0;
		ImageRequest ir = new ImageRequest(getFirst().getView(), getPhotoView());
		load(ir, sampleSize, true);
	}
	
	@Override
	public void loadZoom() {
		getPhotoView().setTag(null);
		String url = getCallback().isLowMemory() ? getFirst().getView() : getFirst().getZoom();
		ImageRequest ir = new ImageRequest(url, getPhotoView());
		load(ir, 0, false);
	}
	
	private void load(ImageRequest ir, int sampleSize, boolean autoScale) {
		ir.setBitmapDisplayer(new PageFadeBitmapDisplayer());
		LowMemoryDecoder lmd = new LowMemoryDecoder(getActivity());
		lmd.setMinimumSampleSize(sampleSize);
		lmd.useAutoScale(autoScale);
		ir.setBitmapDecoder(lmd);
		ir.setBitmapProcessor(new PageBitmapProcessor(getFirstNum()));
		addRequest(ir);
	}
	
}
