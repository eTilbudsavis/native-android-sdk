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

import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.model.Session;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import static android.os.Environment.MEDIA_MOUNTED;

public class ExternalClientIdStore {

    public static final String TAG = Constants.getTag(ExternalClientIdStore.class);

    private static final String CID_RANDOMJUNK = "randomjunkid";

    public static void updateCid(Session s, Context c) {

        String extCid = getCid(c);
        String cid = s.getClientId();

        // Previously used this hardcoded cid in beta, recover it
        if (CID_RANDOMJUNK.equals(cid) || CID_RANDOMJUNK.equals(extCid)) {
            s.setClientId(Utils.createUUID());
            saveCid(s.getClientId(), c);
        } else if (cid == null) {
            // No ClientID is set, try to get from disk
            s.setClientId(extCid);
        } else if (!cid.equals(extCid)) {
            // ClientID have changed, write changes to disk
            saveCid(cid, c);
        }

    }

    private static void saveCid(String cid, Context c) {

        File f = getCidFile(c);
        if (f == null) {
            return;
        }

        FileOutputStream fos = null;
        if (f.exists()) {
            f.delete();
        }
        try {
            fos = new FileOutputStream(f);
            fos.write(cid.getBytes());
            fos.flush();
        } catch (IOException e) {
            // Ignoring
        } finally {
            try {
                fos.close();
            } catch (Throwable t) {

            }
        }

    }

    private static String getCid(Context c) {

        File cidFile = getCidFile(c);
        if (cidFile == null) {
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
        }

        return null;
    }

    private static boolean deleteCid(Context c) {
        File f = getCidFile(c);
        if (f != null && f.exists()) {
            return f.delete();
        }
        return true;
    }

    private static File getCidFile(Context context) {

        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
                PermissionUtils.hasWriteExternalStorage(context)) {

            File cacheDir = new File(Environment.getExternalStorageDirectory(), "cache");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                EtaLog.w(TAG, "External directory couldn't be created");
                return null;
            }

            String fileName = context.getPackageName() + ".txt";
            return new File(cacheDir, fileName);
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
            EtaLog.d(TAG, "ERROR: ClientId has been set: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        String first = "fake_client_id";
        s.setClientId(first);
        updateCid(s, c);
        if (!first.equals(s.getClientId())) {
            EtaLog.d(TAG, "ERROR: ClientId changed: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }
        if (!s.getClientId().equals(extCid)) {
            EtaLog.d(TAG, "ERROR: ClientId mismstch: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        String second = "new_fake_client_id";
        s.setClientId(second);
        updateCid(s, c);
        if (!second.equals(s.getClientId()) || !s.getClientId().equals(extCid)) {
            EtaLog.d(TAG, "ERROR: ClientId mismstch: Session:" + s.getClientId() + ", mCid:" + extCid);
            didFail = true;
        }

        deleteCid(c);
        EtaLog.d(TAG, "Test: " + (didFail ? "failed" : "succeded") + ", in " + (System.currentTimeMillis() - start));

    }

}
