/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.User;

import java.util.List;

public class PermissionUtils {

    public static final String TAG = Constants.getTag(PermissionUtils.class);

    public static final String ERROR_MISSING_PERMISSION = "The user does not have permissions to edit this list";

    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static boolean hasWriteExternalStorage(Context c) {
        return c.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Method that determines if a {@link User} can edit a list of {@link Shoppinglist}
     *
     * @param sl   A {@link Shoppinglist} to check permissions against
     * @param user A {@link User} that will be checked against the Shoppinglist
     */
    public static void allowEditOrThrow(Shoppinglist sl, User user) {
        allowEditOrThrow(sl, user.getEmail());
    }

    /**
     * Method that determines if an email (from user or Share) can edit a list of {@link Shoppinglist}
     *
     * @param sl    A {@link Shoppinglist} to check permissions against
     * @param email An email (representing a {@link User}, or {@link Share}) that will be checked against the {@link Shoppinglist}
     */
    public static void allowEditOrThrow(Shoppinglist sl, String email) {
        if (!allowEdit(sl, email)) {
            throw new IllegalArgumentException(ERROR_MISSING_PERMISSION);
        }
    }

    /**
     * Method that determines if a {@link Share} can edit a list of {@link Shoppinglist}
     *
     * @param sl    A {@link Shoppinglist} to check permissions against
     * @param share A {@link Share} that will be checked against the Shoppinglist
     */
    public static void allowEditOrThrow(Shoppinglist sl, Share share) {
        if (!allowEdit(sl, share)) {
            throw new IllegalArgumentException(ERROR_MISSING_PERMISSION);
        }
    }

    /**
     * Method that determines if a {@link Share} can edit a {@link Shoppinglist#getId()}
     *
     * @param shoppinglistId A {@link Shoppinglist#getId()} to check permissions against
     * @param share          A {@link Share} that will be checked against the Shoppinglist
     */
    public static void allowEditOrThrow(String shoppinglistId, Share share) {
        if (!allowEdit(shoppinglistId, share)) {
            throw new IllegalArgumentException(ERROR_MISSING_PERMISSION);
        }
    }

    /**
     * Method that determines if a {@link User} can edit a list of {@link Shoppinglist}
     *
     * @param list A {@link Shoppinglist} to check edit rights on
     * @param user The {@link User} that wants to edit the {@link Shoppinglist}
     * @return {@code true} if the {@link User} can edit the list of {@link Shoppinglist}, else {@code false}
     */
    public static boolean allowEdit(List<Shoppinglist> list, User user) {
        for (Shoppinglist sl : list) {
            if (!allowEdit(sl, user)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method that determines if a {@link User} can edit a {@link Shoppinglist}
     *
     * @param sl   A {@link Shoppinglist} to check edit rights on
     * @param user The {@link User} that wants to edit the {@link Shoppinglist}
     * @return {@code true} if the {@link User} can edit the {@link Shoppinglist}, else {@code false}
     */
    public static boolean allowEdit(Shoppinglist sl, User user) {
        return allowEdit(sl.getId(), sl.getShares().get(user.getEmail()));
    }

    /**
     * Method that determines if an email (representing a {@link User} or {@link Share})
     * can edit a {@link Shoppinglist}
     *
     * @param sl    A {@link Shoppinglist} to check edit rights on
     * @param email The email (representing a {@link User} or {@link Share}) that wants to edit the {@link Shoppinglist}
     * @return {@code true} if the email can edit the {@link Shoppinglist}, else {@code false}
     */
    public static boolean allowEdit(Shoppinglist sl, String email) {
        return allowEdit(sl.getId(), sl.getShares().get(email));
    }

    /**
     * Method that determines if a {@link Share} has sufficient rights to edit
     * a {@link Shoppinglist}
     *
     * @param sl    A {@link Shoppinglist} to check edit rights on
     * @param share The {@link Share} that wants to edit the {@link Shoppinglist}
     * @return {@code true} if the {@link Share} can edit the list, else {@code false}
     */
    public static boolean allowEdit(Shoppinglist sl, Share share) {
        return allowEdit(sl.getId(), share);
    }

    /**
     * Method that determines if a {@link Share} has sufficient rights to edit
     * a {@link Shoppinglist}
     *
     * @param shoppinglistId A {@link Shoppinglist#getId()} to check edit rights on
     * @param share          The {@link Share} that wants to edit the {@link Shoppinglist}
     * @return {@code true} if the {@link Share} can edit the list, else {@code false}
     */
    public static boolean allowEdit(String shoppinglistId, Share share) {
        if (share == null || shoppinglistId == null) {
            return false;
        }
        if (!share.getShoppinglistId().equals(shoppinglistId)) {
            return false;
        }
        boolean owner = Share.ACCESS_OWNER.equals(share.getAccess());
        boolean rw = Share.ACCESS_READWRITE.equals(share.getAccess());
        return owner || rw;
    }

}
