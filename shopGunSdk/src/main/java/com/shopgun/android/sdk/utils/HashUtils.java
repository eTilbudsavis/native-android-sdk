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

package com.shopgun.android.sdk.utils;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static final String TAG = Constants.getTag(HashUtils.class);

    /**
     * Generate a SHA256 checksum of a string.
     *
     * @param string to SHA256
     * @return A SHA256 string
     */
    public static String sha256(String string) {

        MessageDigest digest = null;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(string.getBytes());
            byte[] bytes = digest.digest();

            StringBuffer sb = new StringBuffer();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            hash = sb.toString();

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hash;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(Integer.toHexString(0xFF & b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return "";
    }

}
