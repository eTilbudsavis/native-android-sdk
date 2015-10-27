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
import android.text.Html;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.api.JsonKeys;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The typeahead allows you to easily define typeahead in {@link AutoCompleteTextView} e.t.c.
 * Further more, this object can 
 * </p>
 *
 * <p>
 * An object matching the responses from typeahead endpoints, in ShopGun API v2.
 * </p>
 */
public class Typeahead implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Typeahead.class);
    public static Parcelable.Creator<Typeahead> CREATOR = new Parcelable.Creator<Typeahead>() {
        public Typeahead createFromParcel(Parcel source) {
            return new Typeahead(source);
        }

        public Typeahead[] newArray(int size) {
            return new Typeahead[size];
        }
    };
    private int mLength = 0;
    private int mOffset = 0;
    private String mSubject;

    /**
     * Default constructor for Typeahead.
     */
    public Typeahead() {
    }

    /**
     * Instantiate a new Typeahead from a {@link String}
     * @param typeahead A {@link String} to use as subject
     */
    public Typeahead(String typeahead) {
        mSubject = typeahead;
    }

    private Typeahead(Parcel in) {
        this.mLength = in.readInt();
        this.mOffset = in.readInt();
        this.mSubject = in.readString();
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Typeahead}
     * @return A {@link List} of POJO
     */
    public static List<Typeahead> fromJSON(JSONArray array) {
        List<Typeahead> list = new ArrayList<Typeahead>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = Json.getObject(array, i);
            if (o != null) {
                list.add(Typeahead.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Typeahead}
     * @return A {@link Typeahead}, or {@code null} if {@code object} is {@code null}
     */
    public static Typeahead fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        Typeahead typeahead = new Typeahead();
        typeahead.setSubject(Json.valueOf(object, JsonKeys.SUBJECT));
        typeahead.setOffset(Json.valueOf(object, JsonKeys.OFFSET, 0));
        typeahead.setLength(Json.valueOf(object, JsonKeys.LENGTH, 0));

        return typeahead;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(JsonKeys.SUBJECT, Json.nullCheck(getSubject()));
            o.put(JsonKeys.LENGTH, getLength());
            o.put(JsonKeys.OFFSET, getOffset());
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return o;
    }

    /**
     * Get the length of the match found for this typeahead
     * @return The length of the match
     */
    public int getLength() {
        return mLength;
    }

    /**
     * Set the length of the match the typeahead has, given a query
     * @param length The length of the match. This may not be longer that the length of the subject
     */
    public void setLength(int length) {
        mLength = length;
    }

    /**
     * Get the offset into the {@link #getSubject() subject} before there is a match
     * @return The offset
     */
    public int getOffset() {
        return mOffset;
    }

    /**
     * Set the offset into the {@link #getSubject() subject} before there is a match
     * @param offset The offset to the match
     */
    public void setOffset(int offset) {
        mOffset = offset;
    }

    /**
     * Get the subject for this {@link Typeahead}.
     * @return A {@link String} is subject have been set, else null
     */
    public String getSubject() {
        return mSubject;
    }

    /**
     * Set the subject of this {@link Typeahead}.
     * When subject is set, offset and length is automatically updated as well.
     *
     * @param subject A {@link String}
     */
    public void setSubject(String subject) {
        mSubject = subject;
    }

    /**
     * This method inserts a set of tags (&lt;b&gt;) to highlight the parts of the subject that
     * was matched by the API, to a given query. The {@link #getSubject() subject} is then returned
     * as a {@link Spanned} object. This type of object can directly be used in a TextView to create
     * the desired effect.
     *
     * @return A {@link Spanned} containing the subject
     */
    public Spanned getHtml() {
        return getHtml("<b>", "</b>");
    }

    /**
     *
     * This method inserts a set of custom tags to highlight the parts of the subject that
     * was matched by the API, to a given query. The {@link #getSubject() subject} is then returned
     * as a {@link Spanned} object. This type of object can directly be used in a TextView to create
     * the desired effect.
     *
     * @param startTag The definition of the start-tag
     * @param endTag The definition of the end-tag
     * @return A {@link Spanned} containing the subject, or {@code null} if subject id null
     */
    public Spanned getHtml(String startTag, String endTag) {

        if (mSubject == null) {
            return null;
        }

        if ((mOffset == 0 && mLength == 0) ||
                mOffset > mSubject.length() ||
                ((mOffset + mLength) > mSubject.length())) {

            // no really good reason to prepend the string with both tags
            return Html.fromHtml(mSubject);

        } else {

            try {

                StringBuilder sb = new StringBuilder();

                int start = mOffset;
                int end = mOffset + mLength;

                if (mOffset > 0) {
                    sb.append(mSubject.substring(0, start));
                }
                sb.append(startTag);
                sb.append(mSubject.substring(start, end));
                sb.append(endTag);
                if (end < mSubject.length()) {
                    sb.append(mSubject.substring(end));
                }
                String html = sb.toString();
                return Html.fromHtml(html);

            } catch (StringIndexOutOfBoundsException e) {
                SgnLog.e(TAG, "", e);
            }

            return Html.fromHtml(mSubject);

        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mLength;
        result = prime * result + mOffset;
        result = prime * result
                + ((mSubject == null) ? 0 : mSubject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Typeahead other = (Typeahead) obj;
        if (mLength != other.mLength)
            return false;
        if (mOffset != other.mOffset)
            return false;
        if (mSubject == null) {
            if (other.mSubject != null)
                return false;
        } else if (!mSubject.equals(other.mSubject))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mLength);
        dest.writeInt(this.mOffset);
        dest.writeString(this.mSubject);
    }

}
