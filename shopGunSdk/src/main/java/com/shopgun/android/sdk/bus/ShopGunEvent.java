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
 * An event from shopgun-android-sdk.
 */
public class ShopGunEvent {

    private Object mTag;

    /**
     * The the {@code tag} associated with this event.
     * @return An {@link Object}, or {@code null}
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Set a {@code tag} to this event.
     * @param tag An {@link Object}, or {@code null}
     */
    public void setTag(Object tag) {
        this.mTag = tag;
    }

    /**
     * Get the type of event.
     * <p>This is equavelent to calling {@link Class#getSimpleName()}</p>
     *
     * @return The type of event
     */
    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getType();
    }
}
