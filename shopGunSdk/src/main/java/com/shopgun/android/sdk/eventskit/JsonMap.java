package com.shopgun.android.sdk.eventskit;

import com.google.gson.internal.bind.util.ISO8601Utils;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonMap extends HashMap<String, Object> {

    public static final String TAG = JsonMap.class.getSimpleName();

    final boolean mOmit;
    final boolean mISO8601;

    public JsonMap() {
        this(true);
    }

    public JsonMap(int capacity) {
        this(capacity, true, true);
    }

    public JsonMap(boolean ommit) {
        this(16, ommit, true);
    }

    public JsonMap(int capacity, boolean ommit, boolean ISO8601) {
        super(capacity);
        mOmit = ommit;
        mISO8601 = ISO8601;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null && mOmit) {
            return remove(key);
        } else if (value instanceof Date) {
            Date tmp = (Date) value;
            value = mISO8601 ? ISO8601Utils.format(tmp) : SgnUtils.dateToString(tmp);
        }
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        for (Map.Entry<? extends String, ? extends Object> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public JSONObject toJson() {
        if (isEmpty() && mOmit) {
            return null;
        }
        return new JSONObject(this);
    }

}
