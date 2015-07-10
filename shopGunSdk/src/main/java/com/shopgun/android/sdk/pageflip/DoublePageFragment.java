/*******************************************************************************
 * Copyright 2015 ShopGun
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

package com.shopgun.android.sdk.pageflip;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.imageloader.ImageRequest;
import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.photoview.PhotoView.OnPhotoDoubleClickListener;
import com.shopgun.android.sdk.photoview.PhotoView.OnPhotoLongClickListener;
import com.shopgun.android.sdk.photoview.PhotoView.OnPhotoTapListener;

import java.util.concurrent.atomic.AtomicInteger;

public class DoublePageFragment extends PageFragment {

    public static final String TAG = Constants.getTag(DoublePageFragment.class);

    private Object LOCK = new Object();
    private AtomicInteger mCount = new AtomicInteger();
    private Bitmap mPage;
    private boolean mClearBitmap = false;

    public void onResume() {
        super.onResume();
        getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {

            public void onPhotoTap(View view, float x, float y) {
                Converter c = new Converter(x, y);
                onSingleClick(c.page, c.x, c.y);
            }
        });
        getPhotoView().setOnPhotoDoubleClickListener(new OnPhotoDoubleClickListener() {

            public void onPhotoTap(View view, float x, float y) {
                Converter c = new Converter(x, y);
                onDoubleClick(c.page, c.x, c.y);
            }
        });
        getPhotoView().setOnPhotoLongClickListener(new OnPhotoLongClickListener() {

            public void onPhotoTap(View view, float x, float y) {
                Converter c = new Converter(x, y);
                onLongClick(c.page, c.x, c.y);
            }
        });
    }

    ;

    private void reset(String tag) {

        synchronized (LOCK) {

            mClearBitmap = true;
            mPage = null;
            mCount = new AtomicInteger();

        }

    }

    @Override
    public void loadView() {
        reset(getFirst().getView());
        int sampleSize = lowMem() ? 2 : 1;
        load(getFirst().getView(), true, sampleSize, true);
        load(getSecond().getView(), false, sampleSize, true);
    }

    @Override
    public void loadZoom() {
        if (lowMem()) {
            return;
        }
        reset(getFirst().getZoom());
        load(getFirst().getZoom(), true, 1, false);
        load(getSecond().getZoom(), false, 1, false);

    }

    private void load(String url, boolean left, int sampleSize, boolean autoScale) {
        ImageRequest r = new ImageRequest(url, new ImageView(getActivity()));
        r.setBitmapDisplayer(new DoublePageDisplayer());
        r.setBitmapProcessor(new DoublePageProcessor(left));
        LowMemoryDecoder lmd = new LowMemoryDecoder(getActivity());
        lmd.setMinimumSampleSize(sampleSize);
        lmd.useAutoScale(autoScale);
        r.setBitmapDecoder(lmd);
        addRequest(r);
    }

    public class DoublePageProcessor extends PageBitmapProcessor {

        private boolean mLeft = true;

        public DoublePageProcessor(boolean leftSide) {
            super(leftSide ? getFirstNum() : getSecondNum());
            mLeft = leftSide;
        }

        public Bitmap process(Bitmap b) {

            synchronized (LOCK) {

                b = super.process(b);

                if (mLeft) {
                    merge(b, null);
                } else {
                    merge(null, b);
                }

				/* 
				 * Recycle the old bitmap, and try to garbage collect...
				 * (garbage collection can't be forced)
				 */
                b.recycle();
                System.gc();

            }
            return b;
        }

        private void merge(Bitmap l, Bitmap r) {

            boolean isLeft = l != null;
            Bitmap b = (isLeft ? l : r);

            createDoublePageIfNeeded(b);

            if (mPage == null) {
                EtaLog.d(TAG, "Can't draw on double-page-bitmap it's null");
            } else if (mPage.isRecycled()) {
                EtaLog.d(TAG, "Can't draw on double-page-bitmap it's recycled");
            } else {
                // Do the paint job
                int left = (isLeft ? 0 : b.getWidth());
                Canvas c = new Canvas(mPage);
                c.drawBitmap(b, left, 0, null);
            }

        }

        private void createDoublePageIfNeeded(Bitmap b) {

            boolean allowRetry = true;
            while (mPage == null && allowRetry) {

                int w = b.getWidth();
                int h = b.getHeight();
                try {
                    mPage = Bitmap.createBitmap(w * 2, h, Config.ARGB_8888);
                } catch (OutOfMemoryError e) {

                    if (allowRetry) {
                        allowRetry = false;
                        EtaLog.e(TAG, e.getMessage(), e);
                        try {
                            // Try to clear up some memory
                            ShopGun.getInstance().getRequestQueue().clear();
                            ShopGun.getInstance().getImageloader().getMemoryCache().clear();
                            // 'force' a GC
                            Runtime.getRuntime().gc();
                            // Wait, and hope for the best
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            EtaLog.e(TAG, "Sleep failed");
                        }
                    } else {
                        throw e;
                    }

                }

            }

        }

    }

//	public class DoublePageDecoder extends LowMemoryDecoder {
//		
//		public DoublePageDecoder(Context c) {
//			super(c);
//		}
//
//		@Override
//		public Bitmap decode(ImageRequest ir, byte[] image) {
//			
//			BitmapFactory.Options o = new BitmapFactory.Options();
//			
//			setMutable(o);
//			
//		    setSampleSize(image, o);
//		    
//		    int ss = (o.inSampleSize<1?1:o.inSampleSize);
//		    int w = (o.outWidth/ss);
//		    int h = (o.outHeight/ss);
//		    
//		    createDoublePageIfNeeded(w, h);
//		    o.inBitmap = mPage;
//		    
//		    // Perform actual decoding
//			Bitmap b = BitmapFactory.decodeByteArray(image, 0, image.length, o);
//			return b;
//		}
//
//		private void createDoublePageIfNeeded(int w, int h) {
//			
//			boolean allowRetry = true;
//			while (mPage==null && allowRetry) {
//				
//				try {
//					mPage = Bitmap.createBitmap(w*2, h, Config.ARGB_8888);
//				} catch (OutOfMemoryError e) {
//					
//					if (allowRetry) {
//						allowRetry = false;
//						EtaLog.e(TAG, e.getMessage(), e);
//						try {
//							// Try to clear up some memory
//							Eta.getInstance().getRequestQueue().clear();
//							Eta.getInstance().getImageloader().getMemoryCache().clear();
//							// 'force' a GC
//							Runtime.getRuntime().gc();
//							// Wait, and hope for the best
//							Thread.sleep(1000);
//						} catch (InterruptedException e1) {
//							EtaLog.e(TAG, "Sleep failed");
//						}
//					} else {
//						throw e;
//					}
//					
//				}
//				
//			}
//			
//		}
//		
//	}

    public class DoublePageDisplayer extends PageFadeBitmapDisplayer {

        @Override
        public void display(ImageRequest ir) {

            if (mCount.getAndIncrement() == 1) {

                // Find the current bitmap, so we can recycle it later
                Bitmap b = null;
                if (mClearBitmap) {
                    mClearBitmap = false;
                    b = getPhotoView().getBitmap();
                }

                // Display the new bitmap while the old one isn't recycled to keep the DisplayMatrix intact
                ir.setBitmap(mPage);
                super.display(ir);

                // Recycle the old bitmap if needed
                if (mClearBitmap && b != null && !b.isRecycled()) {
                    b.recycle();
                }
            }

        }

    }

    private class Converter {
        int page;
        float x;
        float y;

        public Converter(float x, float y) {

            if (x > 0.5f) {
                this.page = getSecondNum();
                this.x = (x - 0.5f) * 2;
                this.y = y;
            } else {
                this.page = getFirstNum();
                this.x = x * 2;
                this.y = y;
            }

        }
    }

}
