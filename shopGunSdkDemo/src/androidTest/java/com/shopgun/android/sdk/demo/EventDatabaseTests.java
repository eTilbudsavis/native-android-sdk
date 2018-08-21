package com.shopgun.android.sdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ApplicationLifecycleCallback;
import android.support.test.runner.lifecycle.ApplicationStage;
import android.test.ApplicationTestCase;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.realm.SgnLegacyEventRealmModule;
import com.shopgun.android.sdk.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * In case AS is not able to install the test.apk, compile the test manually before run them
 * ./gradlew assembleDebugAndroidTest -x test
 *
 * (reference: https://stackoverflow.com/questions/49670865/running-android-test-the-apk-file-androidtest-apk-does-not-exist-on-disk)
 */

/*
 * If you open the .realm file with Realm Studio,
 * it'll create a few files/folder.
 * Delete them and keep only the .realm file or the build will take too long.
 */

@RunWith(AndroidJUnit4.class)
public class EventDatabaseTests extends ApplicationTestCase<Application>{

    private Context context;
    private ShopGun shopGun;

    public EventDatabaseTests() {
        super(Application.class);
    }

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        shopGun = ShopGun.getInstance();
    }

    @After
    public void cleanup() {
        // The testRunner class copies the fake db. Delete it.
        Realm legacy = shopGun.getLegacyRealmInstance();
        if (legacy != null) {
            legacy.close();
            Realm.deleteRealm(legacy.getConfiguration());
        }
    }

    @Test
    public void legacyEventDbExistence() {
        assertEquals(true, shopGun.legacyEventsDetected());
    }

}
