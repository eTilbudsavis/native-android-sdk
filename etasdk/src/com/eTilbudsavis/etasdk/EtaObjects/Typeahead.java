/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IJson;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

/**
 * <p>
 * The typeahead allows you to easily define typeahead in {@link AutoCompleteTextView} e.t.c.
 * Further more, this object can 
 * </p>
 * 
 * <p>
 * An object matching the responses from typeahead endpoints, in eTilbudsavis API v2.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Typeahead implements IJson<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = Eta.TAG_PREFIX + Typeahead.class.getSimpleName();
	
	private int mLength = 0;
	private int mOffset = 0;
	private String mSubject;

	public static Parcelable.Creator<Typeahead> CREATOR = new Parcelable.Creator<Typeahead>(){
		public Typeahead createFromParcel(Parcel source) {
			return new Typeahead(source);
		}
		public Typeahead[] newArray(int size) {
			return new Typeahead[size];
		}
	};

	/**
	 * Default constructor for Typeahead.
	 */
	public Typeahead() { }
	
	/**
	 * Instantiate a new Typeahead from a {@link String}
	 * @param typeahead A {@link String} to use as subject
	 */
	public Typeahead(String typeahead) {
		mSubject = typeahead;
	}

	/**
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param typeaheads A {@link JSONArray} in the format of a valid API v2 typeahead response
	 * @return A {@link List} of POJO
	 */
	public static List<Typeahead> fromJSON(JSONArray typeaheads) {
		List<Typeahead> resp = new ArrayList<Typeahead>();
		try {
			for (int i = 0 ; i < typeaheads.length() ; i++ ) {
				resp.add(Typeahead.fromJSON((JSONObject)typeaheads.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return resp;
	}
	
	/**
	 * A factory method for converting {@link JSONObject} into POJO.
	 * <p>This method exposes a way, of updating/setting an objects properties</p>
	 * @param typeahead An object to set/update
	 * @param jTypeahead A {@link JSONObject} in the format of a valid API v2 typeahead response
	 * @return A {@link List} of POJO
	 */
	public static Typeahead fromJSON(JSONObject jTypeahead) {
		Typeahead typeahead = new Typeahead();
		if (jTypeahead == null) {
			return typeahead;
		}
		
		typeahead.setSubject(Json.valueOf(jTypeahead, JsonKey.SUBJECT));
		typeahead.setOffset(Json.valueOf(jTypeahead, JsonKey.OFFSET, 0));
		typeahead.setLength(Json.valueOf(jTypeahead, JsonKey.LENGTH, 0));
		
		return typeahead;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.SUBJECT, Json.nullCheck(getSubject()));
			o.put(JsonKey.LENGTH, getLength());
			o.put(JsonKey.OFFSET, getOffset());
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
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
	 * Set the offset of this {@link Typeahead}.
	 * @param offset
	 */
	public void setOffset(int offset) {
		mOffset = offset;
	}
	
	/**
	 * Get the subject for this Typeahead.
	 * @return A {@link String} is subject have been set, else null
	 */
	public String getSubject() {
		return mSubject;
	}
	
	/**
	 * Set the subject of this Typeahead. When subject is set, offset and length is automatically updated as well.
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
	 * This method inserts a set of custom tags to highlight the parts of the subject that
	 * was matched by the API, to a given query. The {@link #getSubject() subject} is then returned
	 * as a {@link Spanned} object. This type of object can directly be used in a TextView to create
	 * the desired effect.
	 * 
	 * @return A {@link Spanned} containing the subject, or {@code null} if
	 *          subject id null
	 */
	public Spanned getHtml(String startTag, String endTag) {
		
		if (mSubject == null) {
			return null;
		}
		
		if ( (mOffset == 0 && mLength == 0) ||
				mOffset > mSubject.length() ||
				( (mOffset + mLength) > mSubject.length() ) ) {
			
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
				EtaLog.e(TAG, "", e);
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

	private Typeahead(Parcel in) {
		this.mLength = in.readInt();
		this.mOffset = in.readInt();
		this.mSubject = in.readString();
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
