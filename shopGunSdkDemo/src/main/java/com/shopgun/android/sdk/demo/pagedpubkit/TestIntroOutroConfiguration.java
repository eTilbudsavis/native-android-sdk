package com.shopgun.android.sdk.demo.pagedpubkit;

import android.content.res.Configuration;
import android.os.Parcel;
import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.pagedpublicationkit.PagedPublication;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationHotspotCollection;
import com.shopgun.android.sdk.pagedpublicationkit.PagedPublicationPage;
import com.shopgun.android.sdk.pagedpublicationkit.impl.IntroOutroConfiguration;
import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoSpreadProperty;

import junit.framework.Assert;

import java.util.List;

public class TestIntroOutroConfiguration extends IntroOutroConfiguration {

    int mPublicationPageCount;
    Orientation mOrientation;
    boolean mIntro;
    boolean mOutro;

    public TestIntroOutroConfiguration(int publicationPageCount, Orientation orientation, boolean intro, boolean outro) {
        mPublicationPageCount = publicationPageCount;
        mOrientation = orientation;
        mIntro = intro;
        mOutro = outro;
    }

    @Override
    public int getPublicationPageCount() {
        return mPublicationPageCount;
    }

    @Override
    public Orientation getOrientation() {
        return mOrientation;
    }

    @Override
    public boolean hasIntro() {
        return mIntro;
    }

    @Override
    public boolean hasOutro() {
        return mOutro;
    }

    @Override
    public View getPublicationPageView(ViewGroup container, int publicationPage) {
        return null;
    }

    @Override
    public VersoSpreadProperty getPublicationSpreadProperty(int spreadPosition, int[] pages) {
        return null;
    }

    @Override
    public View getIntroPageView(ViewGroup container, int page) {
        Assert.assertTrue(hasIntro());
        return super.getIntroPageView(container, page);
    }

    @Override
    public VersoSpreadProperty getIntroSpreadProperty(int spreadPosition, int[] pages) {
        Assert.assertTrue(hasIntro());
        return super.getIntroSpreadProperty(spreadPosition, pages);
    }

    @Override
    public View getIntroSpreadOverlay(ViewGroup container, int[] pages) {
        Assert.assertTrue(hasIntro());
        return super.getIntroSpreadOverlay(container, pages);
    }

    @Override
    public VersoSpreadProperty getOutroSpreadProperty(int spreadPosition, int[] pages) {
        Assert.assertTrue(hasOutro());
        return super.getOutroSpreadProperty(spreadPosition, pages);
    }

    @Override
    public View getOutroPageView(ViewGroup container, int page) {
        Assert.assertTrue(hasOutro());
        return super.getOutroPageView(container, page);
    }

    @Override
    public View getOutroSpreadOverlay(ViewGroup container, int[] pages) {
        Assert.assertTrue(hasOutro());
        return super.getOutroSpreadOverlay(container, pages);
    }

    @Override
    public PagedPublication getPublication() {
        return null;
    }

    @Override
    public boolean hasPublication() {
        return false;
    }

    @Override
    public List<? extends PagedPublicationPage> getPages() {
        return null;
    }

    @Override
    public boolean hasPages() {
        return false;
    }

    @Override
    public PagedPublicationHotspotCollection getHotspotCollection() {
        return null;
    }

    @Override
    public boolean hasHotspotCollection() {
        return false;
    }

    @Override
    public void load(OnLoadComplete callback) {

    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = Orientation.fromConfiguration(newConfig);
    }

    @Override
    public int getSpreadMargin() {
        return 0;
    }

    @Override
    public boolean hasData() {
        return false;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPublicationPageCount);
        dest.writeInt(this.mOrientation == null ? -1 : this.mOrientation.ordinal());
    }

    public TestIntroOutroConfiguration() {
    }

    protected TestIntroOutroConfiguration(Parcel in) {
        this.mPublicationPageCount = in.readInt();
        int tmpMOrientation = in.readInt();
        this.mOrientation = tmpMOrientation == -1 ? null : Orientation.values()[tmpMOrientation];
    }

    public static final Creator<TestIntroOutroConfiguration> CREATOR = new Creator<TestIntroOutroConfiguration>() {
        @Override
        public TestIntroOutroConfiguration createFromParcel(Parcel source) {
            return new TestIntroOutroConfiguration(source);
        }

        @Override
        public TestIntroOutroConfiguration[] newArray(int size) {
            return new TestIntroOutroConfiguration[size];
        }
    };
}
