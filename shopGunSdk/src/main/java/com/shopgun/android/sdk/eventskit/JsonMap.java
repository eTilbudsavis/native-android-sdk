package com.shopgun.android.sdk.eventskit;

import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JsonMap extends HashMap<String, Object> {

    public static final String TAG = JsonMap.class.getSimpleName();

    final boolean mOmit;

    public JsonMap() {
        this(true);
    }

    public JsonMap(int capacity) {
        this(capacity, true);
    }

    public JsonMap(boolean ommit) {
        mOmit = ommit;
    }

    public JsonMap(int capacity, boolean ommit) {
        super(capacity);
        mOmit = ommit;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == null && mOmit) {
            return remove(key);
        } else if (value instanceof Date) {
            Date tmp = (Date) value;
            value = SgnUtils.dateToString(tmp);
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
