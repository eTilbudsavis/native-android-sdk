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

import java.util.regex.Pattern;

public class Validator {

    public static final String TAG = Constants.getTag(Validator.class);

    public static final String APP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([+-][0-9A-Za-z-.]*)?";

    public static final String xAPP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([-]([0-9A-Za-z-.]+)*)?";

    //           \d+\.\d+\.\d+(\-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?

    /**
     * <p>A very naive implementation of email validation. Requirement:</p>
     * <ul>
     *      <li>Email != null</li>
     *      <li>must contain a single '@' char</li>
     *      <li>At least one char before and after the '@'</li>
     *      <li>email.trim() must be equal to email</li>
     * </ul>
     *
     * <p>But why not just use an email RegEx, like android.util.Patterns.EMAIL_ADDRESS?</p>
     *
     * <p>Simple: it doesn't comply with RFC 2822. But neither does uur {@link Validator}.
     * We do something better, we'll let the API decide if it's valid.</p>
     *
     * @param email A string to check
     * @return {@code true} if email is valid, else {@code false}
     */
    public static boolean isEmailValid(String email) {

        if (email == null) {
            return false;
        }

        String[] split = email.split("@");
        if (split.length != 2) {
            return false;
        }
        if (split[0].length() == 0 || split[1].length() == 0) {
            return false;
        }

        return email.trim().equals(email);
    }

    /**
     * A simple regular expression to check if the app-version string can be accepted by the API
     *
     * @param version to check
     * @return true, if the version matched the regex
     */
    public static boolean isAppVersionValid(String version) {
        return version != null && Pattern.compile(APP_VERSION_FORMAT).matcher(version).matches();
    }

}
