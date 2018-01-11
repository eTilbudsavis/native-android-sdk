package com.shopgun.android.sdk.demo.pagedpubkit;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.CatalogConfiguration;
import com.shopgun.android.verso.VersoPageView;
import com.shopgun.android.verso.VersoPageViewFragment;

public class IntroOutroCatalogConfiguration extends CatalogConfiguration {

    boolean mIntro = false;
    boolean mOutro = false;

    public IntroOutroCatalogConfiguration(Catalog catalog, boolean intro, boolean outro) {
        super(catalog.getId());
        mIntro = intro;
        mOutro = outro;
    }

    @Override
    public boolean hasIntro() {
        return mIntro;
    }

    @Override
    public View getIntroPageView(ViewGroup container, int page) {
        TextView tv = getTextView(container);
        tv.setText("IntroView for:\n" + getCatalog().getBranding().getName());
        return tv;
    }

    @Override
    public boolean hasOutro() {
        return mOutro;
    }

    @Override
    public View getOutroPageView(ViewGroup container, int page) {
        TextView tv = getTextView(container);
        tv.setText("Outro for:\n" + getCatalog().getBranding().getName());
        return tv;
    }

    private TextView getTextView(ViewGroup container) {
        TextView tv = new IntroOutroView(container.getContext());
        tv.setBackgroundColor(Color.YELLOW);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(30f);
        return tv;
    }

    static class IntroOutroView extends TextView implements VersoPageView {

        public IntroOutroView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }

        @Override
        public boolean onZoom(float scale) {
            return false;
        }

        @Override
        public int getPage() {
            return 0;
        }

        @Override
        public void setOnLoadCompleteListener(VersoPageViewFragment.OnLoadCompleteListener listener) {

        }

        @Override
        public void onVisible() {

        }

        @Override
        public void onInvisible() {

        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.mIntro ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mOutro ? (byte) 1 : (byte) 0);
    }

    protected IntroOutroCatalogConfiguration(Parcel in) {
        super(in);
        this.mIntro = in.readByte() != 0;
        this.mOutro = in.readByte() != 0;
    }

    public static final Creator<IntroOutroCatalogConfiguration> CREATOR = new Creator<IntroOutroCatalogConfiguration>() {
        @Override
        public IntroOutroCatalogConfiguration createFromParcel(Parcel source) {
            return new IntroOutroCatalogConfiguration(source);
        }

        @Override
        public IntroOutroCatalogConfiguration[] newArray(int size) {
            return new IntroOutroCatalogConfiguration[size];
        }
    };

}