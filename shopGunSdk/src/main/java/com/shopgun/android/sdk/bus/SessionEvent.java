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

/**
 * Created by Danny Hvam - danny@etilbudsavis.dk on 13/05/15.
 */
public class SessionEvent extends ShopGunEvent {

    private int mOldUser = 0;
    private int mNewUser = 0;

    public SessionEvent(int oldUser, int newUser) {
        this.mOldUser = oldUser;
        this.mNewUser = newUser;
    }

    public boolean isNewUser() {
        return mOldUser != mNewUser;
    }

    public int getOldUser() {
        return mOldUser;
    }

    public int getNewUser() {
        return mNewUser;
    }

    @Override
    public String toString() {
        return String.format("%s[ oldUser: %s, newUser: %s ]", getType(), mOldUser, mNewUser);
    }
}
