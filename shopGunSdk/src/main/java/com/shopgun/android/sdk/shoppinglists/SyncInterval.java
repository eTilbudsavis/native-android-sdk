package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;

/**
 * @deprecated No longer maintained
 * Supported synchronization intervals for {@link SyncManager}
 * and is one of:
 * <ul>
 *     <li>SLOW</li>
 *     <li>MEDIUM</li>
 *     <li>FAST</li>
 *     <li>PAUSED</li>
 * </ul>
 * Please only use {@link SyncInterval#FAST} when needed, e.g.: when the user is actively interacting
 * with a {@link Shoppinglist} or it's {@link ShoppinglistItem}'s.
 */
@Deprecated
public class SyncInterval {
    public static int SLOW = 10000;
    public static int MEDIUM = 6000;
    public static int FAST = 3000;
    public static int PAUSED = -1;
}
