package com.tjek.sdk.publicationviewer.paged.verso;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shopgun.android.sdk.R;
import com.tjek.sdk.publicationviewer.paged.NumberUtils;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayout;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayoutEvent;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomLayoutInterface;
import com.tjek.sdk.publicationviewer.paged.zoomlayout.ZoomOnDoubleTapListener;

import java.util.Arrays;
import java.util.HashSet;

@SuppressWarnings("unused")
public class VersoPageViewFragment extends Fragment {

    public static final String TAG = VersoPageViewFragment.class.getSimpleName();

    private static final String KEY_POSITION = "position";

    public static VersoPageViewFragment newInstance(int position) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_POSITION, position);
        VersoPageViewFragment fragment = new VersoPageViewFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    // Views
    private ZoomLayout mZoomLayout;
    private VersoHorizontalLayout mPageContainer;
    private View mSpreadOverlay;
    private OverlaySizer mOverlaySizer;

    // Input data
    private VersoSpreadConfiguration mVersoSpreadConfiguration;
    private VersoSpreadProperty mProperty;
    protected int mPosition;
    protected int[] mPages;

    // listeners
    private VersoPageViewInterface.EventListener mVersoPageViewEventListener;
    private VersoPageViewInterface.OnLoadCompleteListener mOnLoadCompleteListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(KEY_POSITION);
            mProperty = mVersoSpreadConfiguration.getSpreadProperty(mPosition);
            mPages = mProperty.getPages();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mZoomLayout = (ZoomLayout) inflater.inflate(R.layout.verso_page_layout, container, false);

        // scale operations on large bitmaps are horrible slow
        // for some reason, this works. LAYER_TYPE_SOFTWARE works too...
        mZoomLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        mZoomLayout.addZoomLayoutEventListener(new ZoomLayoutEventDispatcher());

        boolean zoom = !NumberUtils.isEqual(mProperty.getMaxZoomScale(), mProperty.getMinZoomScale());
        mZoomLayout.setAllowZoom(zoom);
        mZoomLayout.setMinScale(mProperty.getMinZoomScale());
        mZoomLayout.setMaxScale(mProperty.getMaxZoomScale());

        mZoomLayout.setZoomDuration(180);

        mPageContainer = mZoomLayout.findViewById(R.id.verso_pages_container);
        mSpreadOverlay = mVersoSpreadConfiguration.getSpreadOverlay(mZoomLayout, mPages);
        if (mSpreadOverlay != null) {
            mZoomLayout.addView(mSpreadOverlay);
        }

        return mZoomLayout;

    }

    @Override
    public void onStart() {
        super.onStart();
        addVersoPageViews();
        mOverlaySizer = new OverlaySizer();
        mPageContainer.addOnLayoutChangeListener(mOverlaySizer);
    }

    private void addVersoPageViews() {
        for (int page : mPages) {
            View view = mVersoSpreadConfiguration.getPageView(mPageContainer, page);
            try {
                ((VersoPageView)view).setOnLoadCompleteListener(mOnLoadCompleteListener);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("The view must implement VersoPageView", e);
            }
            mPageContainer.addView(view);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mPageContainer.removeOnLayoutChangeListener(mOverlaySizer);
        mOverlaySizer = null;
        mPageContainer.removeAllViews();
    }

    public ZoomLayout getZoomLayout() {
        return mZoomLayout;
    }

    private class OverlaySizer implements View.OnLayoutChangeListener {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            boolean changed = left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom;
            if (changed && mSpreadOverlay != null) {
                Rect r = getChildPosition();
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSpreadOverlay.getLayoutParams();
                lp.width = r.width();
                lp.height = r.height();
                lp.gravity = Gravity.CENTER;
                mSpreadOverlay.setLayoutParams(lp);
            }
        }
    }

    private Rect getChildPosition() {
        Rect rect = new Rect();
        int childCount = mPageContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = mPageContainer.getChildAt(i);
            if (i == 0) {
                // First item, just set the rect
                rect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            } else {
                if (rect.left > child.getLeft()) {
                    rect.left = child.getLeft();
                }
                if (rect.top > child.getTop()) {
                    rect.top = child.getTop();
                }
                if (rect.right < child.getRight()) {
                    rect.right = child.getRight();
                }
                if (rect.bottom < child.getBottom()) {
                    rect.bottom = child.getBottom();
                }
            }
        }
        return rect;
    }

    public View getSpreadOverlay() {
        return mSpreadOverlay;
    }

    public int getSpreadPosition() {
        return mPosition;
    }

    public int[] getPages() {
        return Arrays.copyOf(mPages, mPages.length);
    }

    public void setVersoSpreadConfiguration(VersoSpreadConfiguration configuration) {
        mVersoSpreadConfiguration = configuration;
    }

    public void setVersoPageViewEventListener(VersoPageViewInterface.EventListener l) {
        mVersoPageViewEventListener = l;
    }

    public void setOnLoadCompleteListener(VersoPageViewInterface.OnLoadCompleteListener listener) {
        mOnLoadCompleteListener = listener;
    }

    public void dispatchZoom(float scale) {
        int count = mPageContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            final View v = mPageContainer.getChildAt(i);
            if (v instanceof VersoPageView) {
                ((VersoPageView)v).onZoom(scale);
            }
        }
    }

    public boolean isScaled() {
        return mZoomLayout.isScaled();
    }

    public boolean isScaling() {
        return mZoomLayout.isScaling();
    }

    public boolean isTranslating() {
        return mZoomLayout.isTranslating();
    }

    public void getVisiblePages(Rect bounds, HashSet<Integer> result) {
        if (mZoomLayout == null) {
            return;
        }
        Rect mHitBounds = new Rect();
        int[] pos = new int[2];
        int count = mPageContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            final View v = mPageContainer.getChildAt(i);
            v.getHitRect(mHitBounds);
            v.getLocationOnScreen(pos);
            mHitBounds.offsetTo(pos[0], pos[1]);
            if (Rect.intersects(bounds, mHitBounds)) {
                result.add(mPages[i]);
            }
        }
    }

    protected void dispatchPageVisibilityChange(int[] added, int[] removed) {
        if (!isAdded()) {
            // If scrolling is too fast, the MessageQueue (or something related) can't keep up, and we crash...
            return;
        }
        for (int i = 0; i < mPageContainer.getChildCount(); i++) {
            View v = mPageContainer.getChildAt(i);
            int page = mPages[i];
            if (v instanceof VersoPageView) {
                VersoPageView pv = (VersoPageView) v;
                for (int a : added) if (a == page) pv.onVisible();
                for (int r : removed) if (r == page) pv.onInvisible();
            }
        }
    }

    private Rect getZoomLayoutRect(ZoomLayout zl) {
        RectF r = zl.getDrawRect();
        return new Rect(Math.round(r.left), Math.round(r.top), Math.round(r.right), Math.round(r.bottom));
    }

    // Takes events from ZoomLayout, transform them into VersoPageViewEvent and propagate them
    private class ZoomLayoutEventDispatcher implements ZoomLayoutInterface {

        private final ZoomLayoutInterface mZoomDoubleTapListener = new ZoomOnDoubleTapListener(false);

        @Override
        public boolean onZoomLayoutEvent(@NonNull ZoomLayoutEvent event) {
            if (mVersoPageViewEventListener == null)
                return false;
            if (event instanceof ZoomLayoutEvent.Touch) {
                ZoomLayoutEvent.Touch e = (ZoomLayoutEvent.Touch) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.Touch(e.getAction(), new VersoTapInfo(e.getInfo(), VersoPageViewFragment.this))
                );
            }
            if (event instanceof ZoomLayoutEvent.Tap) {
                ZoomLayoutEvent.Tap e = (ZoomLayoutEvent.Tap) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.Tap(new VersoTapInfo(e.getInfo(), VersoPageViewFragment.this))
                );
            }
            if (event instanceof ZoomLayoutEvent.DoubleTap) {
                ZoomLayoutEvent.DoubleTap e = (ZoomLayoutEvent.DoubleTap) event;
                boolean consumed = mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.DoubleTap(new VersoTapInfo(e.getInfo(), VersoPageViewFragment.this)));
                return !consumed && mZoomDoubleTapListener.onZoomLayoutEvent(event);
            }
            if (event instanceof ZoomLayoutEvent.LongTap) {
                ZoomLayoutEvent.LongTap e = (ZoomLayoutEvent.LongTap) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.LongTap(new VersoTapInfo(e.getInfo(), VersoPageViewFragment.this))
                );
            }
            if (event instanceof ZoomLayoutEvent.ZoomBegin) {
                ZoomLayoutEvent.ZoomBegin e = (ZoomLayoutEvent.ZoomBegin) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.ZoomBegin(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getScale(), getZoomLayoutRect(e.getView())))
                );
            }
            if (event instanceof ZoomLayoutEvent.Zoom) {
                ZoomLayoutEvent.Zoom e = (ZoomLayoutEvent.Zoom) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.Zoom(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getScale(), getZoomLayoutRect(e.getView())))
                );
            }
            if (event instanceof ZoomLayoutEvent.ZoomEnd) {
                ZoomLayoutEvent.ZoomEnd e = (ZoomLayoutEvent.ZoomEnd) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.ZoomEnd(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getScale(), getZoomLayoutRect(e.getView())))
                );
            }
            if (event instanceof ZoomLayoutEvent.PanBegin) {
                ZoomLayoutEvent.PanBegin e = (ZoomLayoutEvent.PanBegin) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.PanBegin(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getView().getScale(), getZoomLayoutRect(e.getView())))
                );
            }
            if (event instanceof ZoomLayoutEvent.Pan) {
                ZoomLayoutEvent.Pan e = (ZoomLayoutEvent.Pan) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.Pan(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getView().getScale(), getZoomLayoutRect(e.getView())))
                );
            }
            if (event instanceof ZoomLayoutEvent.PanEnd) {
                ZoomLayoutEvent.PanEnd e = (ZoomLayoutEvent.PanEnd) event;
                return mVersoPageViewEventListener.onVersoPageViewEvent(
                        new VersoPageViewEvent.PanEnd(new VersoZoomPanInfo(VersoPageViewFragment.this, e.getView().getScale(), getZoomLayoutRect(e.getView())))
                );
            }

            return false;
        }

    }
}
