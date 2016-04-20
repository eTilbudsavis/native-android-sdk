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
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.User;

import java.util.List;

public class PermissionUtils {

    public static final String TAG = Constants.getTag(PermissionUtils.class);

    public static final String ERROR_MISSING_PERMISSION = "User doesn't have edit permissions, reason: %s";

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
            String reason = getReasonForNotAllowEdit(sl, email);
            String message = String.format(ERROR_MISSING_PERMISSION, reason);
            throw new IllegalArgumentException(message);
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
            String reason = getReasonForNotAllowEdit(sl, share);
            String message = String.format(ERROR_MISSING_PERMISSION, reason);
            throw new IllegalArgumentException(message);
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
            String reason = getReasonForNotAllowEdit(shoppinglistId, share);
            String message = String.format(ERROR_MISSING_PERMISSION, reason);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Method that determines if a {@link User} can edit a list of {@link Shoppinglist}
     *
     * @param list A {@link Shoppinglist} to check edit rights on
     * @param user The {@link User} that wants to edit the {@link Shoppinglist}
     */
    public static void allowEditOrThrow(List<Shoppinglist> list, User user) {
        if (!allowEdit(list, user)) {
            String reason = getReasonForNotAllowEdit(list, user);
            String message = String.format(ERROR_MISSING_PERMISSION, reason);
            throw new IllegalArgumentException(message);
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
        if (list == null || user == null) {
            return false;
        }
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
        return sl != null && sl.getShares() != null && user != null && allowEdit(sl.getId(), sl.getShares().get(user.getEmail()));
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
        return sl != null && sl.getShares() != null && allowEdit(sl.getId(), sl.getShares().get(email));
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
        return sl != null && allowEdit(sl.getId(), share);
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
        if (!shoppinglistId.equals(share.getShoppinglistId())) {
            return false;
        }
        return hasWritePermission(share);
    }

    private static boolean hasWritePermission(Share share) {
        boolean owner = Share.ACCESS_OWNER.equals(share.getAccess());
        boolean rw = Share.ACCESS_READWRITE.equals(share.getAccess());
        return owner || rw;
    }

    /**
     * Get a reason for why a given edit isn't allowed.
     *
     * @param list A {@link List} of {@link Shoppinglist} to check
     * @param user A {@link User} to check if can edit the given {@link List} of {@link Shoppinglist}
     * @return A reason for not allowing edit access.
     */
    public static String getReasonForNotAllowEdit(List<Shoppinglist> list, User user) {
        if (list == null) {
            return "list == null";
        } else if (user == null) {
            return "user == null";
        } else {
            for (Shoppinglist sl : list) {
                if (!allowEdit(sl, user)) {
                    return getReasonForNotAllowEdit(sl, user);
                }
            }
        }
        return "Unknown reason";
    }

    /**
     * Get a reason for why a given edit isn't allowed.
     *
     * @param sl A {@link Shoppinglist} to check
     * @param user A {@link User} to check if can edit the given {@link Shoppinglist}
     * @return A reason for not allowing edit access.
     */
    public static String getReasonForNotAllowEdit(Shoppinglist sl, User user) {
        return getReasonForNotAllowEdit(sl, user.getEmail());
    }

    /**
     * Get a reason for why a given edit isn't allowed.
     *
     * @param sl A {@link Shoppinglist} to check
     * @param share A {@link Share} to check if can edit the given {@link Shoppinglist}
     * @return A reason for not allowing edit access.
     */
    public static String getReasonForNotAllowEdit(Shoppinglist sl, Share share) {
        if (sl == null) {
            return "Shoppinglist == null";
        }
        return getReasonForNotAllowEdit(sl.getId(), share);
    }

    /**
     * Get a reason for why a given edit isn't allowed.
     *
     * @param sl A {@link Shoppinglist} to check
     * @param email An email to check if can edit the given {@link Shoppinglist}
     * @return A reason for not allowing edit access.
     */
    public static String getReasonForNotAllowEdit(Shoppinglist sl, String email) {
        if (sl == null) {
            return "Shoppinglist == null";
        } else if (sl.getShares() == null) {
            return "Shoppinglist does not contain any shares (Shoppinglist.getShares() == null)";
        }
        return getReasonForNotAllowEdit(sl.getId(), sl.getShares().get(email));
    }

    /**
     * Get a reason for why a given edit isn't allowed.
     *
     * @param shoppinglistId A {@link Shoppinglist#getId() Shoppinglist.id} to check
     * @param share A {@link Share} to check if can edit the given {@link Shoppinglist}
     * @return A reason for not allowing edit access.
     */
    public static String getReasonForNotAllowEdit(String shoppinglistId, Share share) {
        if (share == null) {
            return "Share == null";
        } else if (shoppinglistId == null) {
            return "Shoppinglist.id == null";
        } else if (!shoppinglistId.equals(share.getShoppinglistId())) {
            return "Shoppinglist.id != Share.getShoppinglistId()";
        } else if (!hasWritePermission(share)) {
            String f = "Share (%s) does not have write permissions (access == %s)";
            return String.format(f, share.getEmail(), share.getAccess());
        }
        return "Unknown reason";
    }
}
