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
import android.support.annotation.Keep;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Keep
public class Permission implements IJson<JSONObject>, Parcelable {

    public static final String TAG = Constants.getTag(Permission.class);
    public static final Parcelable.Creator<Permission> CREATOR = new Parcelable.Creator<Permission>() {
        public Permission createFromParcel(Parcel source) {
            return new Permission(source);
        }

        public Permission[] newArray(int size) {
            return new Permission[size];
        }
    };
    private HashMap<String, ArrayList<String>> mPermissions = new HashMap<String, ArrayList<String>>();

    public Permission() {

    }

    private Permission(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            ArrayList<String> value = new ArrayList<String>();
            in.readStringList(value);
            mPermissions.put(key, value);
        }
    }

    /**
     * Convert a {@link JSONArray} into a {@link List};.
     * @param array A {@link JSONArray}  with a valid API v2 structure for a {@code Permission}
     * @return A {@link List} of POJO
     */
    public static List<Permission> fromJSON(JSONArray array) {
        List<Permission> list = new ArrayList<Permission>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.optJSONObject(i);
            if (o != null) {
                list.add(Permission.fromJSON(o));
            }
        }
        return list;
    }

    /**
     * A factory method for converting {@link JSONObject} into a POJO.
     * @param object A {@link JSONObject} with a valid API v2 structure for a {@code Links}
     * @return A {@link Links}, or {@code null} if {@code object} is {@code null}
     */
    public static Permission fromJSON(JSONObject object) {
        if (object == null) {
            return null;
        }

        Permission p = new Permission();
        try {

            JSONArray groups = object.names();
            if (groups == null) {
                return p;
            }

            for (int i = 0; i < groups.length(); i++) {

                String group = groups.get(i).toString();
                JSONArray jArray = object.getJSONArray(group);
                ArrayList<String> permissions = new ArrayList<String>();

                for (int j = 0; j < jArray.length(); j++) {
                    permissions.add(jArray.get(j).toString());
                }

                p.getPermissions().put(group, permissions);

            }

        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }

        return p;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            Iterator<String> it = getPermissions().keySet().iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (it.hasNext()) {
                JSONArray jArray = new JSONArray();
                String name = it.next();
                for (String value : getPermissions().get(name)) {
                    jArray.put(value);
                }
                o.put(name, jArray);
            }
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return o;
    }

    public ArrayList<String> getGroupPermissions(String group) {
        return mPermissions.get(group);
    }

    public Permission put(String group, ArrayList<String> permissions) {
        if (mPermissions.containsKey(group)) {
            mPermissions.get(group).addAll(permissions);
        } else {
            mPermissions.put(group, permissions);
        }
        return this;
    }

    public HashMap<String, ArrayList<String>> getPermissions() {
        return mPermissions;
    }

    public Permission putAll(HashMap<String, ArrayList<String>> permissions) {
        mPermissions.putAll(permissions);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mPermissions == null) ? 0 : mPermissions.hashCode());
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
        Permission other = (Permission) obj;
        if (mPermissions == null) {
            if (other.mPermissions != null)
                return false;
        } else if (!mPermissions.equals(other.mPermissions))
            return false;
        return true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPermissions.size());
        for (Map.Entry<String, ArrayList<String>> e : mPermissions.entrySet()) {
            dest.writeString(e.getKey());
            dest.writeStringList(e.getValue());
        }
    }

}
