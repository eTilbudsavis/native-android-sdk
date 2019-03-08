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

package com.shopgun.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Keep
public class Images implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Images.class);
    private String mView;
    private String mZoom;
    private String mThumb;

    public Images() {

    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for an images
     * @return A {@link List} of POJO
     */
    public static List<Images> fromJSON(JSONArray array) {
        List<Images> list = new ArrayList<Images>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for an images
     * @return A {@link Images}, or {@code null} if {@code object} is {@code null}
     */
    public static Images fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }
        SgnJson o = new SgnJson(object);
        Images i = new Images()
                .setView(o.getView())
                .setZoom(o.getZoom())
                .setThumb(o.getThumb());

        o.getStats().ignoreForgottenKeys("@note.1").log(TAG);

        return i;
    }

    public JSONObject toJSON() {
        return new SgnJson()
                .setView(getView())
                .setZoom(getZoom())
                .setThumb(getThumb())
                .toJSON();
    }

    public String getView() {
        return mView;
    }

    public Images setView(String viewUrl) {
        this.mView = viewUrl;
        return this;
    }

    public String getZoom() {
        return mZoom;
    }

    public Images setZoom(String zoomUrl) {
        this.mZoom = zoomUrl;
        return this;
    }

    public String getThumb() {
        return mThumb;
    }

    public Images setThumb(String thumbUrl) {
        this.mThumb = thumbUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Images images = (Images) o;

        if (mView != null ? !mView.equals(images.mView) : images.mView != null) return false;
        if (mZoom != null ? !mZoom.equals(images.mZoom) : images.mZoom != null) return false;
        return !(mThumb != null ? !mThumb.equals(images.mThumb) : images.mThumb != null);

    }

    @Override
    public int hashCode() {
        int result = mView != null ? mView.hashCode() : 0;
        result = 31 * result + (mZoom != null ? mZoom.hashCode() : 0);
        result = 31 * result + (mThumb != null ? mThumb.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mView);
        dest.writeString(this.mZoom);
        dest.writeString(this.mThumb);
    }

    protected Images(Parcel in) {
        this.mView = in.readString();
        this.mZoom = in.readString();
        this.mThumb = in.readString();
    }

    public static final Creator<Images> CREATOR = new Creator<Images>() {
        public Images createFromParcel(Parcel source) {
            return new Images(source);
        }

        public Images[] newArray(int size) {
            return new Images[size];
        }
    };
}
