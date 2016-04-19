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

package com.shopgun.android.sdk;

import android.content.Context;
import android.os.Environment;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Session;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.Utils;

import java.io.File;
import java.io.RandomAccessFile;

import static android.os.Environment.MEDIA_MOUNTED;

public class ExternalClientIdStore {

    public static final String TAG = Constants.getTag(ExternalClientIdStore.class);

    private static final String CID_RANDOMJUNK = "randomjunkid";

    public static void updateCid(Session s, Context c) {
        String extCid = getCid(c);
        if (CID_RANDOMJUNK.equals(s.getClientId()) || CID_RANDOMJUNK.equals(extCid)) {
            // Previously used this hardcoded cid in beta, recover it
            s.setClientId(Utils.createUUID());
        } else if (s.getClientId() == null) {
            // No ClientID is set, try to get from disk
            s.setClientId(extCid);
        }
        ShopGun.getInstance(c).getSettings().setClientId(s.getClientId());
    }

    private static String getCid(Context c) {

        SgnLog.printStackTrace(TAG);
        // First try SharedPrefs
        ShopGun sgn = ShopGun.getInstance(c);
        SgnLog.d(TAG, "getCid.2");
        Settings settings = sgn.getSettings();
        SgnLog.d(TAG, "getCid.3");
        String cid = settings.getClientId();
        SgnLog.d(TAG, "getCid.4");

        if (cid != null) {
            SgnLog.d(TAG, "getCid.sharedPreferences");
            return cid;
        }

        // Then try external storage
        File cidFile = getCidFile(c);
        if (cidFile == null) {
            SgnLog.d(TAG, "getCid.cidFile == null");
            return null;
        }

        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(cidFile, "r");
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                return null;
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return new String(data);
        } catch (Exception e) {
            // Ignore
        } finally {
            try {
                f.close();
            } catch (Exception e) {
                // Ignore
            }

            // Cleanup the cid file, we won't need it any more
            deleteCid(c);
        }

        return null;
    }

    private static boolean deleteCid(Context c) {
        SgnLog.d(TAG, "deleteCid");
        File f = getCidFile(c);
        return f != null && f.exists() && f.delete();
    }

    private static File getCidFile(Context context) {

        SgnLog.d(TAG, "getCidFile");

        try {
            if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                    PermissionUtils.hasWriteExternalStorage(context)) {

                File cacheDir = new File(Environment.getExternalStorageDirectory(), "cache");
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    SgnLog.w(TAG, "External directory couldn't be created");
                    return null;
                }

                String fileName = context.getPackageName() + ".txt";
                return new File(cacheDir, fileName);
            }
        } catch (Exception e) {
            // If we are not allowed to access external storage
        }

        return null;
    }

    public static void test(Context c) {

        long start = System.currentTimeMillis();

        // Just clearing the prefs file
        deleteCid(c);

        boolean didFail = false;

        String extCid = null;

        // no CID has been obtained yet
        Session s = new Session();
        updateCid(s, c);
        if (s.getClientId() != null || extCid != null) {
            SgnLog.d(TAG, "ERROR: ClientId has been set: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        String first = "fake_client_id";
        s.setClientId(first);
        updateCid(s, c);
        if (!first.equals(s.getClientId())) {
            SgnLog.d(TAG, "ERROR: ClientId changed: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }
        if (!s.getClientId().equals(extCid)) {
            SgnLog.d(TAG, "ERROR: ClientId mismstch: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        String second = "new_fake_client_id";
        s.setClientId(second);
        updateCid(s, c);
        if (!second.equals(s.getClientId()) || !s.getClientId().equals(extCid)) {
            SgnLog.d(TAG, "ERROR: ClientId mismstch: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        deleteCid(c);
        SgnLog.d(TAG, "Test: " + (didFail ? "failed" : "succeded") + ", in " + (System.currentTimeMillis() - start));

    }

}
