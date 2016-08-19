package com.shopgun.android.sdk.corekit.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.shopgun.android.sdk.utils.SgnUtils;

import java.lang.reflect.Type;
import java.util.Date;

public class DateSerializer implements JsonSerializer<Date> {

    private boolean mIso8601 = false;
    private boolean mOmmit = false;

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return mOmmit ? null : JsonNull.INSTANCE;
        } else {
            String s = mIso8601 ? ISO8601Utils.format(src) : SgnUtils.dateToString(src);
            return new JsonPrimitive(s);
        }
    }

}
