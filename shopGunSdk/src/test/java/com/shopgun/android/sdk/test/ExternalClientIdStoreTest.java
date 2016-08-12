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

package com.shopgun.android.sdk.test;

import android.content.Context;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Session;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.ExternalClientIdStore;
import com.shopgun.android.sdk.utils.SgnUtils;

import junit.framework.TestCase;

public class ExternalClientIdStoreTest extends TestCase {

    public static final String TAG = Constants.getTag(ExternalClientIdStoreTest.class);

    public static void test(Context ctx) {

        SdkTest.start(TAG);

        ShopGun sgn = ShopGun.getInstance();
        String current = sgn.getSettings().getClientId();

        // Just clearing the prefs file
        ExternalClientIdStore.clear(sgn);

        // no CID has been obtained yet
        Session s = new Session();
        ExternalClientIdStore.updateCid(s, sgn);

        assertNull(s.getClientId());
        assertNull(sgn.getSettings().getClientId());

        String first = "fake_client_id";
        s.setClientId(first);
        ExternalClientIdStore.updateCid(s, sgn);
        assertEquals(first, s.getClientId());
        assertEquals(first, sgn.getSettings().getClientId());

        String second = SgnUtils.createUUID();
        s.setClientId(second);
        ExternalClientIdStore.updateCid(s, sgn);

        assertEquals(second, s.getClientId());
        assertEquals(second, sgn.getSettings().getClientId());

        // new session, doesn't have a client_id, but should restore from storage
        s = new Session();
        ExternalClientIdStore.updateCid(s, sgn);
        assertEquals(second, s.getClientId());

        ExternalClientIdStore.clear(sgn);

        // Test randomjunk recovery
        String junk = "randomjunkid";
        s = new Session();
        s.setClientId(junk);
        ExternalClientIdStore.updateCid(s, sgn);
        assertNotSame(junk, s.getClientId());

        sgn.getSettings().setClientId(current);

    }

}
