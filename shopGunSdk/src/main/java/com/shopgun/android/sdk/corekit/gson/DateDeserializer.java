package com.shopgun.android.sdk.corekit.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.shopgun.android.sdk.utils.SgnUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

public class DateDeserializer implements JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        } else {
            String date = json.getAsString();
            try {
                return SgnUtils.stringToDateThrows(date);
            } catch (ParseException e) {
                // ignore
            }
            try {
                return ISO8601Utils.parse(date, new ParsePosition(0));
            } catch (ParseException e) {
                // ignore
            }
            return null;
        }
    }

}
