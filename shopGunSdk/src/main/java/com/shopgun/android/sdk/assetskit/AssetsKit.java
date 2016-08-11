package com.shopgun.android.sdk.assetskit;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.shopgun.android.sdk.Constants;

import java.io.File;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AssetsKit {

    public static final String TAG = Constants.getTag(AssetsKit.class);

    private static final String URL = "https://assets.shopgun.com";
    private static final HttpUrl URL_PARSED = HttpUrl.parse(URL);

    private OkHttpClient mClient;

    public Call newFileUpload(File file) {

        Uri uri = Uri.fromFile(file);
        String mimeType = getMimeType(uri.toString());
        MediaType mediaType = MediaType.parse(mimeType);
        RequestBody body = new MultipartBody.Builder()
                .setType(mediaType)
                .addPart(RequestBody.create(mediaType, file))
                .build();

        Request request = new Request.Builder()
                .url(URL_PARSED)
                .header("Content-Type", "which/mimetype")
                .header("Accept", "application/json")
                .post(body)
                .build();

        return mClient.newCall(request);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
