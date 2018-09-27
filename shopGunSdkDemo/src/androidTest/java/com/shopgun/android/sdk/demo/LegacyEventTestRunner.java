package com.shopgun.android.sdk.demo;

import android.app.Application;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;

import com.shopgun.android.sdk.corekit.realm.SgnLegacyEventRealmModule;
import com.shopgun.android.sdk.utils.Constants;

import org.junit.runner.RunWith;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Custom runner that creates a legacy event db before the Application class starts.
 */

@RunWith(AndroidJUnit4.class)
public class LegacyEventTestRunner extends AndroidJUnitRunner {

    @Override
    public void callApplicationOnCreate(Application app) {

        /* Note: getTargetContext needs to be used, so the fake db must live in the asset folder of the app */
        Context context = InstrumentationRegistry.getTargetContext();

        String REALM_NAME = Constants.PACKAGE + ".realm";

        Realm.init(context);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name(REALM_NAME)
                .modules(new SgnLegacyEventRealmModule())
                .schemaVersion(1)
                .build();


        // Copy the stored version 1 realm file from assets to a NEW location.
        // Note: the old file is always deleted for you by the copyRealmFromAssets
        TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
        try {
            configFactory.copyRealmFromAssets(context, REALM_NAME, realmConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.callApplicationOnCreate(app);
    }
}
