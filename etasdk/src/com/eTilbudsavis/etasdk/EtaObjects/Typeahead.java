package com.eTilbudsavis.etasdk.EtaObjects;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
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
public class Typeahead extends EtaObject {

	public static final String TAG = "Typeahead";
	
	private int mLength = 0;
	private int mOffset = 0;
	private String mSubject;

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
	 * A factory method for converting {@link JSONArray} into a {@link List} of Typeahead.
	 * 
	 * @param typeaheads A {@link JSONArray} containing API v2 typeahead objects
	 * @return A {@link List} of Typeahead converted from the {@link JSONArray}
	 */
	public static List<Typeahead> fromJSON(JSONArray typeaheads) {
		List<Typeahead> list = new ArrayList<Typeahead>();
		try {
			for (int i = 0 ; i < typeaheads.length() ; i++ ) {
				list.add(Typeahead.fromJSON((JSONObject)typeaheads.get(i)));
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}

	/**
	 * A factory method for converting JSON into POJO.
	 * 
	 * @param typeaheads A {@link JSONArray} containing API v2 typeahead objects
	 * @return A {@link List} of {@link Typeahead} converted from the {@link JSONArray}
	 */
	public static Typeahead fromJSON(JSONObject storeTypeahead) {
		return fromJSON(new Typeahead(), storeTypeahead );
	}
	
	private static Typeahead fromJSON(Typeahead typeahead, JSONObject t) {
		if (typeahead == null) typeahead = new Typeahead();
		if (t == null) return typeahead;
		
		typeahead.setSubject(Json.valueOf(t, ServerKey.SUBJECT));
		typeahead.setOffset(Json.valueOf(t, ServerKey.OFFSET, 0));
		typeahead.setLength(Json.valueOf(t, ServerKey.LENGTH, 0));
		
		return typeahead;
	}
	
	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	/**
	 * Static method for converting object into {@link JSONObject}, same as {@link EtaObject#toJSON() toJson()}
	 * @see EtaObject#toJSON()
	 * @param typeahead A object to convert
	 * @return A {@link JSONObject} representation of the typeahead
	 */
	public static JSONObject toJSON(Typeahead typeahead) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.SUBJECT, Json.nullCheck(typeahead.getSubject()));
			o.put(ServerKey.LENGTH, typeahead.getLength());
			o.put(ServerKey.OFFSET, typeahead.getOffset());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
		int max = mSubject.length() - mOffset;
		mLength = (length > max ? max : length);
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
		mOffset = ( offset > mSubject.length() ) ? mSubject.length() : mOffset;
		setLength(mLength);
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
		// Remember to update these bastards
		setOffset(mOffset);
		setLength(mLength);
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
	 * @return A {@link Spanned} containing the subject
	 */
	public Spanned getHtml(String startTag, String endTag) {
		
		if (mSubject == null) {
			return null;
		}
		
		if (mOffset == 0 && mLength == 0) {
			
			// no really good reason to prepend the string with both tags
			return Html.fromHtml(mSubject);
			
		} else {
			
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
			
		}
		
	}
	
}
