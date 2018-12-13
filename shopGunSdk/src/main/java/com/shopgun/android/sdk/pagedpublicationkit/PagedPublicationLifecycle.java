package com.shopgun.android.sdk.pagedpublicationkit;

import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.utils.NumberUtils;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogUtil;
import com.shopgun.android.verso.VersoZoomPanInfo;

class PagedPublicationLifecycle implements Parcelable {

    public static final String TAG = PagedPublicationLifecycle.class.getSimpleName();

    private boolean mDebug = false;

    PagedPublicationConfiguration mConfig;

    private boolean mOpened = false;
    private boolean mAppeared = false;
    private boolean mResumed = false;
    private boolean[] mPageAppeared;
    private boolean[] mPageLoaded;
    private boolean[] mPageLoadedTmp;
    private boolean[] mSpreadAppeared;
    private boolean[] mSpreadZoomedIn;
    private boolean[] mSpreadZoomedInTmp;

    PagedPublicationLifecycle() {
    }

    public void setConfig(PagedPublicationConfiguration config) {
        mConfig = config;
    }

    private boolean isReady() {
        if (mConfig == null || !mConfig.hasData()) {
            log("Event called before configuration is ready");
            return false;
        }
        if (mPageAppeared == null) {
            ensureArrays(mConfig.getPageCount(), mConfig.getSpreadCount());
        }
        return true;
    }

    private boolean isReadyAndResumed() {
        if (isReady()) {
            if (mResumed) {
                return true;
            }
            log("Lifecycle not resumed - event likely ignored");
        }
        return false;
    }

    private void log(String msg) {
        if (mDebug) {
            LogUtil.printStackTrace(TAG, 4, 100);
            L.d(TAG, msg);
        }
    }

    private boolean indexIsInRange(final int length, final int index) {
        return (index >= 0 && index < length);
    }

    public void ensureArrays(int pageCount, int spreadCount) {
        mPageAppeared = new boolean[pageCount];
        mPageLoaded = new boolean[pageCount];
        mPageLoadedTmp = new boolean[pageCount];
        mSpreadAppeared = new boolean[spreadCount];
        mSpreadZoomedIn = new boolean[spreadCount];
        mSpreadZoomedInTmp = new boolean[spreadCount];
    }

    public void resetSpreadsPagesLoadedAndZoom() {
        mPageAppeared = null;
        mPageLoaded = null;
        mPageLoadedTmp = null;
        mSpreadAppeared = null;
        mSpreadZoomedIn = null;
        mSpreadZoomedInTmp = null;
    }

    void resumed() {
        mResumed = true;
        appeared();
    }

    void paused() {
        disappeared();
        mResumed = false;
    }

    void opened() {
        if (isReady() && !mOpened) {
            mOpened = true;
            PagedPublicationEvent.opened(mConfig).track();
        }
    }

    void appeared() {
        opened();
        if (isReadyAndResumed() && !mAppeared) {
            mAppeared = true;
        }
    }

    void disappeared() {
        if (isReady() && mAppeared) {
            mAppeared = false;
            for (int i = 0; i < mSpreadAppeared.length; i++) {
                spreadDisappeared(i, mConfig.getPagesFromSpreadPosition(i));
            }
        }
    }

    void pageAppeared(int page) {
        if (isReadyAndResumed() && indexIsInRange(mPageAppeared.length, page) && !mPageAppeared[page]) {
            int sp = mConfig.getSpreadPositionFromPage(page);
            if (mSpreadAppeared[sp]) {
                mPageAppeared[page] = true;
                if (indexIsInRange(mPageLoaded.length, page) && mPageLoaded[page]) {
                    pageLoaded(page);
                }
            } else {
                L.d(TAG, "Page " + page + " appeared before it's spread");
            }
        }
    }

    void pageDisappeared(int page) {
        if (isReady() && indexIsInRange(mPageAppeared.length, page) && mPageAppeared[page] && indexIsInRange(mPageLoaded.length, page)) {
            mPageAppeared[page] = false;
            mPageLoaded[page] = false;

            // pages start from 0
            page++;
            // exclude the outro page.
            // Note: currently we don't have any catalog with intro
            int pageLimit = mConfig.hasOutro() ? mConfig.getPageCount() - 1 : mConfig.getPageCount();
            if (page <= pageLimit) {
                PagedPublicationEvent.pageDisappeared(mConfig, page).track();
            }
        }
    }

    void spreadAppeared(int spread, int[] pageNumbers, boolean callPagesAppear) {
        if (isReadyAndResumed() && mAppeared && indexIsInRange(mSpreadAppeared.length, spread) && !mSpreadAppeared[spread]) {
            mSpreadAppeared[spread] = true;
            if (callPagesAppear) {
                for (int page : pageNumbers) {
                    pageAppeared(page);
                }
            }
            if (indexIsInRange(mSpreadZoomedInTmp.length, spread) && mSpreadZoomedInTmp[spread]) {
                internalSpreadZoomedIn(spread, pageNumbers);
                mSpreadZoomedInTmp[spread] = false;
            }
        }
    }

    void spreadDisappeared(int spread, int[] pageNumbers) {
        if (isReady() && indexIsInRange(mSpreadAppeared.length, spread) && mSpreadAppeared[spread]) {
            spreadZoomedOut(spread, pageNumbers, 1.0f);
            for (int i : pageNumbers) {
                pageDisappeared(i);
            }
            mSpreadAppeared[spread] = false;
        }
    }

    void spreadZoomedIn(VersoZoomPanInfo info) {
        spreadZoomedIn(info.getPosition(), info.getPages(), info.getScale());
    }

    void spreadZoomedIn(int spread, int[] pages, float scale) {
        if (isReadyAndResumed() && indexIsInRange(mSpreadZoomedIn.length, spread) && !mSpreadZoomedIn[spread] && scale > 1.0f) {
            internalSpreadZoomedIn(spread, pages);
        }
    }

    private void internalSpreadZoomedIn(int spread, int[] pages) {
        if (isReadyAndResumed() && indexIsInRange(mSpreadZoomedIn.length, spread)) {
            mSpreadZoomedIn[spread] = true;
        }
    }

    void spreadZoomedOut(VersoZoomPanInfo info) {
        spreadZoomedOut(info.getPosition(), info.getPages(), info.getScale());
    }

    void spreadZoomedOut(int spread, int[] pages, float scale) {
        if (isReady() && indexIsInRange(mSpreadZoomedIn.length, spread) && mSpreadZoomedIn[spread] && NumberUtils.isEqual(1.0f, scale)) {
            mSpreadZoomedIn[spread] = false;
        }
    }

    void saveState() {
        try {
            System.arraycopy(mPageLoaded, 0, mPageLoadedTmp, 0, mPageLoadedTmp.length);
            System.arraycopy(mSpreadZoomedIn, 0, mSpreadZoomedInTmp, 0, mSpreadZoomedInTmp.length);
        } catch (IndexOutOfBoundsException ignore) { }
    }

    void applyState(int spread, int[] pages) {
        for (int page : pages) {
            mPageLoaded[page] = mPageLoaded[page] | mPageLoadedTmp[page];
        }
        mSpreadZoomedIn[spread] = mSpreadZoomedIn[spread] | mSpreadZoomedInTmp[spread];
    }

    void pageLoaded(int page) {
        if (isReady() && indexIsInRange(mPageLoaded.length, page)) {
            mPageLoaded[page] = true;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mOpened ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mAppeared ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mResumed ? (byte) 1 : (byte) 0);
        dest.writeBooleanArray(this.mPageAppeared);
        dest.writeBooleanArray(this.mPageLoaded);
        dest.writeBooleanArray(this.mPageLoadedTmp);
        dest.writeBooleanArray(this.mSpreadAppeared);
        dest.writeBooleanArray(this.mSpreadZoomedIn);
        dest.writeBooleanArray(this.mSpreadZoomedInTmp);
    }

    protected PagedPublicationLifecycle(Parcel in) {
        this.mOpened = in.readByte() != 0;
        this.mAppeared = in.readByte() != 0;
        this.mResumed = in.readByte() != 0;
        this.mPageAppeared = in.createBooleanArray();
        this.mPageLoaded = in.createBooleanArray();
        this.mPageLoadedTmp = in.createBooleanArray();
        this.mSpreadAppeared = in.createBooleanArray();
        this.mSpreadZoomedIn = in.createBooleanArray();
        this.mSpreadZoomedInTmp = in.createBooleanArray();
    }

    public static final Creator<PagedPublicationLifecycle> CREATOR = new Creator<PagedPublicationLifecycle>() {
        @Override
        public PagedPublicationLifecycle createFromParcel(Parcel source) {
            return new PagedPublicationLifecycle(source);
        }

        @Override
        public PagedPublicationLifecycle[] newArray(int size) {
            return new PagedPublicationLifecycle[size];
        }
    };
}
