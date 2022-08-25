package com.tjek.sdk.demo;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.util.Log;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.realm.SgnLegacyEventRealmModule;
import com.shopgun.android.sdk.eventskit.LegacyEventDispatcher;
import com.shopgun.android.sdk.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;

/**
 * In case AS is not able to install the test.apk, compile the test manually before run them
 * ./gradlew :shopGunSdkDemo:assembleDebugAndroidTest -x test
 *
 * (reference: https://stackoverflow.com/questions/49670865/running-android-test-the-apk-file-androidtest-apk-does-not-exist-on-disk)
 */

/*
 * If you open the .realm file with Realm Studio,
 * it'll create a few files/folder.
 * Delete them and keep only the .realm file or the build will take too long.
 */

@RunWith(AndroidJUnit4.class)
public class LegacyEventDispatcherTests {

    private Context context;
    private ShopGun shopGun;

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

    @Test
    public void testLegacyDispatcher() throws IOException {
        MockWebServer server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setBody(getIds()).setResponseCode(HttpURLConnection.HTTP_OK));
        HttpUrl baseUrl = server.url("/");

        LegacyEventDispatcher legacyEventDispatcher = new LegacyEventDispatcher(shopGun.getClient(), baseUrl.toString());

        legacyEventDispatcher.start();

        try {
            while(legacyEventDispatcher.isAlive()) {

                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
                if (request != null) {
                    Log.d("TEST -----> testLegacyDispatcher", request.getBody().toString());
                }
                else {
                    break;
                }
            }

        } catch (InterruptedException e) {
            assertEquals("", "takeRequest timer expired.");
            e.printStackTrace();
        }
        finally {
            legacyEventDispatcher.quit();
        }

        // check if the legacy event db is still there
        // Note: we cannot use Realm.getInstance or it'll create the db again
        RealmConfiguration legacyConfiguration = new RealmConfiguration.Builder()
                .name(Constants.PACKAGE + ".realm")
                .modules(new SgnLegacyEventRealmModule())
                .schemaVersion(1)
                .build();

        assertEquals(false, new File(legacyConfiguration.getPath()).exists());

        server.shutdown();
    }

    private String getIds() {
        String response = "{\"events\": [" +
                "{\"id\": \"event-1\", \"status\": \"ack\", \"errors\": []}," +
                "{\"id\": \"event-2\", \"status\": \"ack\", \"errors\": []}," +
                "{\"id\": \"event-3\", \"status\": \"ack\", \"errors\": []}" +
                "]}";
        return response;
    }

}
