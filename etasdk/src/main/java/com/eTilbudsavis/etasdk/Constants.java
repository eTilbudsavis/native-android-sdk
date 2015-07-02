package com.eTilbudsavis.etasdk;

public class Constants {

    public static final String META_API_KEY = "com.eTilbudsavis.etasdk.api_key";
    public static final String META_API_SECRET = "com.eTilbudsavis.etasdk.api_secret";

    public static final String META_DEVELOP_API_KEY = "com.eTilbudsavis.etasdk.develop.api_key";
    public static final String META_DEVELOP_API_SECRET = "com.eTilbudsavis.etasdk.develop.api_secret";

    public static final String TAG_PREFIX = "Etasdk-";
    public static final String ARG_PREFIX = "com.eTilbudsavis.etasdk.";

    public static String getTag(Class<?> clazz) {
        return getTag(clazz.getSimpleName());
    }

    public static String getTag(String tag) {
        return TAG_PREFIX + tag;
    }

    public static String getArg(String arg) {
        return ARG_PREFIX + arg;
    }

}
