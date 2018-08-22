package com.shopgun.android.sdk.demo;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.realm.SgnLegacyEventRealmModule;
import com.shopgun.android.sdk.eventskit.AnonymousEvent;
import com.shopgun.android.sdk.eventskit.AnonymousEventWrapper;
import com.shopgun.android.sdk.eventskit.EventDispatcher;
import com.shopgun.android.sdk.eventskit.LegacyEventDispatcher;
import com.shopgun.android.sdk.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.LinkedBlockingQueue;
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
public class EventDispatcherTests {

    private Context context;
    private ShopGun shopGun;

    private String id1;
    private String id2;
    private String id3;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        shopGun = ShopGun.getInstance();

        AnonymousEvent e1 = new AnonymousEvent(AnonymousEvent.DEFAULT_TYPE);
        AnonymousEvent e2 = new AnonymousEvent(AnonymousEvent.DEFAULT_TYPE);
        AnonymousEvent e3 = new AnonymousEvent(AnonymousEvent.DEFAULT_TYPE);

        id1 = e1.getId();
        id2 = e2.getId();
        id3 = e3.getId();

        Realm realm = shopGun.getRealmInstance();
        realm.beginTransaction();
        realm.insert(new AnonymousEventWrapper(e1.getId(), e1.getVersion(), e1.getTimestamp(), e1.toString()));
        realm.insert(new AnonymousEventWrapper(e2.getId(), e2.getVersion(), e2.getTimestamp(), e2.toString()));
        realm.insert(new AnonymousEventWrapper(e3.getId(), e3.getVersion(), (e3.getTimestamp() - TimeUnit.DAYS.toSeconds(8)), e3.toString()));
        realm.commitTransaction();
        realm.close();
    }

    @After
    public void cleanup() {
        // The testRunner class copies the fake db. Delete it.
//        Realm realm = shopGun.getRealmInstance();
//        if (realm != null) {
//            realm.close();
//            Realm.deleteRealm(realm.getConfiguration());
//        }
    }

    @Test
    public void testDispatcher() throws IOException {
        MockWebServer server = new MockWebServer();
        server.start();
        server.enqueue(new MockResponse().setBody(getIds()).setResponseCode(HttpURLConnection.HTTP_OK));
        HttpUrl baseUrl = server.url("/");


        EventDispatcher eventDispatcher = new EventDispatcher(new LinkedBlockingQueue<AnonymousEvent>(10), shopGun.getClient(), baseUrl.toString());

        eventDispatcher.start();

        while(eventDispatcher.isAlive()){
            try {
                RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
                if (request != null) {
                    Log.d("TEST", request.getBody().toString());
                }
            } catch (InterruptedException ignore) {}
        }

        // 1 event shipped, 1 to be retransmitted, 1 too old and deleted
        Realm realm = shopGun.getRealmInstance();
        assertEquals(1, realm.where(AnonymousEventWrapper.class).count());


        server.shutdown();
    }

    private String getIds() {
        String response = String.format("{\"events\": [" +
                "{\"id\": \"%s\", \"status\": \"ack\", \"errors\": []}," +
                "{\"id\": \"%s\", \"status\": \"nack\", \"errors\": []}," +
                "{\"id\": \"%s\", \"status\": \"nack\", \"errors\": []}" +
                "]}", id1, id2, id3);
        return response;
    }

}
