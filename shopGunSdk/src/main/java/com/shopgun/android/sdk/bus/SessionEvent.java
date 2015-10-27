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

package com.shopgun.android.sdk.bus;

import com.shopgun.android.sdk.model.User;

/**
 *
 */
public class SessionEvent extends ShopGunEvent {

    private int mOldUser = 0;
    private int mNewUser = 0;

    public SessionEvent(int oldUser, int newUser) {
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
    }

    /**
     * Check if the event is caused by a new user being logged in.
     * @return {@code true} if it's a new user, else {@code false}
     */
    public boolean isNewUser() {
        return mOldUser != mNewUser;
    }

    /**
     * Get the {@link User#getUserId() id} of the {@link User} that was logged in previous to this event.
     * @return An {@link User#getUserId() id}
     */
    public int getOldUser() {
        return mOldUser;
    }

    /**
     * Get the {@link User#getUserId() id} of the {@link User} that is currently logged in.
     * Notice that a {@link SessionEvent} doesn't have to be a change of user.
     * @return An {@link User#getUserId() id}
     */
    public int getNewUser() {
        return mNewUser;
    }

    @Override
    public String toString() {
        return String.format("%s[ oldUser: %s, newUser: %s ]", getType(), mOldUser, mNewUser);
    }
}
