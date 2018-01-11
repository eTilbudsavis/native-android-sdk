package com.shopgun.android.sdk.pagedpublicationkit.impl;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationConfiguration;
import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoSpreadProperty;

import java.util.Arrays;

public abstract class IntroOutroConfiguration implements PagedPublicationConfiguration {

    public static final String TAG = IntroOutroConfiguration.class.getSimpleName();
    
    @Override
    public int getPageCount() {
        int count = getPublicationPageCount();
        if (count > 0) {
            if (hasIntro()) count++;
            if (hasOutro()) count++;
        }
        return count;
    }

    @Override
    public int getSpreadCount() {
        int count = getPublicationPageCount();
        if (count > 0) {
            count = getOrientation().isLandscape() ? (count/2)+1 : count;
            if (hasIntro()) count++;
            if (hasOutro()) count++;
        }
        return count;
    }

    @Override
    public int getSpreadPositionFromPage(int page) {
        if (getOrientation().isPortrait()) {
            return page;
        }
        if (page == 0 || (hasIntro() && page == 1)) {
            return page;
        }
        if (hasOutro() && page == getPageCount()-1) {
            return getSpreadCount()-1;
        } else if (hasIntro()) {
            return ((page - (page % 2))/2)+1;
        } else {
            return ((page-1)/2)+1;
        }
    }

    @Override
    public int[] getPagesFromSpreadPosition(int position) {

        if (getOrientation().isPortrait()) {
            return new int[]{ position };
        }

        if (position == 0 || (hasIntro() && position == 1)) {
            // either intro or first page of catalog
            return new int[]{ position };
        }

        int page = hasIntro() ? (position - 1) * 2 : (position * 2) - 1;
        if (hasOutro() && position == getSpreadCount()-1 && !missingLastPage()) {
            page--;
        }

        int lastDoublePage = getSpreadCount() - 1;
        lastDoublePage -= hasOutro() ? 2 : 1;
        if (missingLastPage()) {
            lastDoublePage++;
        }
        boolean isSinglePage = position > lastDoublePage;
        return isSinglePage ? new int[]{ page } : new int[]{ page, page+1 };

    }

    @NonNull
    @Override
    public View getPageView(ViewGroup container, int page) {
        if (hasIntro() && page == 0) {
            return getIntroPageView(container, page);
        } else if (hasOutro() && page == getPageCount() - 1) {
            return getOutroPageView(container, page);
        } else {
            // Offset the page by one if there is an intro to get
            // the publicationPage, rather than the verso page
            int publicationPage = hasIntro() ? page - 1 : page;
            return getPublicationPageView(container, publicationPage);
        }
    }

    @Override
    public VersoSpreadProperty getSpreadProperty(int spreadPosition) {
        int[] pages = getPagesFromSpreadPosition(spreadPosition);
        if (hasIntro() && spreadPosition == 0) {
            return getIntroSpreadProperty(spreadPosition, pages);
        } else if (hasOutro() && spreadPosition == getSpreadCount()-1 ) {
            return getOutroSpreadProperty(spreadPosition, pages);
        } else {
            return getPublicationSpreadProperty(spreadPosition, pages);
        }
    }

    @Override
    public View getSpreadOverlay(ViewGroup container, int[] pages) {
        int position = getSpreadPositionFromPage(pages[0]);
        if (hasIntro() && position == 0) {
            return getIntroSpreadOverlay(container, pages);
        } else if (hasOutro() && position == getSpreadCount()-1 ) {
            return getOutroSpreadOverlay(container, pages);
        } else {
            return getPublicationSpreadOverlay(container, fixPages(pages));
        }
    }

    public abstract int getPublicationPageCount();

    public abstract Orientation getOrientation();

    public abstract View getPublicationPageView(ViewGroup container, int publicationPage);

    @Override
    public boolean hasIntro() {
        return false;
    }

    @Override
    public boolean hasOutro() {
        return false;
    }

    @Override
    public View getIntroPageView(ViewGroup container, int page) {
        return null;
    }

    @Override
    public View getOutroPageView(ViewGroup container, int page) {
        return null;
    }

    public abstract VersoSpreadProperty getPublicationSpreadProperty(int spreadPosition, int[] pages);

    public VersoSpreadProperty getIntroSpreadProperty(int spreadPosition, int[] pages) {
        return null;
    }

    public VersoSpreadProperty getOutroSpreadProperty(int spreadPosition, int[] pages) {
        return null;
    }

    public View getPublicationSpreadOverlay(ViewGroup container, int[] publicationPages) {
        return null;
    }

    public View getIntroSpreadOverlay(ViewGroup container, int[] pages) {
        return null;
    }

    public View getOutroSpreadOverlay(ViewGroup container, int[] pages) {
        return null;
    }

    private int[] fixPages(int[] pages) {
        int[] tmp = Arrays.copyOf(pages, pages.length);
        if (hasIntro()) {
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] -= 1;
            }
        }
        return tmp;
    }

    private boolean missingLastPage() {
        return getPublicationPageCount() % 2 != 0;
    }

}
