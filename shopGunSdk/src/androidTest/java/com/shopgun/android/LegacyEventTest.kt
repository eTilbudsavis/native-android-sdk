package com.shopgun.android

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.tjek.sdk.legacy.AnonymousEventWrapper
import com.tjek.sdk.legacy.LegacyEventRealmModule
import com.tjek.sdk.database.TjekRoomDb
import com.tjek.sdk.eventstracker.TjekEventsTracker
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegacyEventTest {

    private lateinit var context: Context
    private lateinit var realmConfiguration: RealmConfiguration

    private val legacyEvents = listOf(
        AnonymousEventWrapper("e14276a2-74cd-42bd-a082-5b577a574c76", 2, 1660130180, "{\"_v\":2,\"_i\":\"e14276a2-74cd-42bd-a082-5b577a574c76\",\"_e\":12,\"_t\":1660130180,\"_av\":\"6.5.2\",\"c\":\"screen\",\"a\":\"opened\",\"os\":\"android\",\"osv\":\"12\",\"d\":\"Google Pixel 3 XL\",\"s\":\"homeScreen\",\"vt\":\"9JRmAmEumSg=\"}"),
        AnonymousEventWrapper("ac8acf26-8e1c-4114-8384-19d55003c1ca", 2, 1660130180, "{\"_v\":2,\"_i\":\"ac8acf26-8e1c-4114-8384-19d55003c1ca\",\"_e\":12,\"_t\":1660130180,\"_av\":\"6.5.2\",\"c\":\"screen\",\"a\":\"opened\",\"os\":\"android\",\"osv\":\"12\",\"d\":\"Google Pixel 3 XL\",\"s\":\"searchScreen\",\"vt\":\"9JRmAmEumSg=\"}"),
        AnonymousEventWrapper("0fba6fab-f6a8-439a-a92a-402a73a6448f", 2, 1660130185, "{\"_v\":2,\"_i\":\"0fba6fab-f6a8-439a-a92a-402a73a6448f\",\"_e\":12,\"_t\":1660130185,\"_av\":\"6.5.2\",\"c\":\"screen\",\"a\":\"opened\",\"os\":\"android\",\"osv\":\"12\",\"d\":\"Google Pixel 3 XL\",\"s\":\"listScreen\",\"vt\":\"f5VXkWcVLMc=\"}"),
    )

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        TjekRoomDb.getInstance(context).clearAllTables()

        Realm.init(context)
        realmConfiguration = RealmConfiguration.Builder()
            .name("com.shopgun.android.sdk.anonymousEvent.realm")
            .modules(LegacyEventRealmModule())
            .schemaVersion(2)
            .build().also {
                try {
                    // delete whatever we have
                    Realm.deleteRealm(it)
                    val realm = Realm.getInstance(it)
                    // insert some data
                    legacyEvents.forEach { e ->
                        realm.executeTransaction { r -> r.insert(e) }
                    }
                    realm.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @Test
    fun testLegacyEventConversion() {
        TjekEventsTracker.initialize(context)
        // give a bit time to the coroutine to insert data
        Thread.sleep(200)
        val dao = TjekRoomDb.getInstance(context).eventDao()
        runBlocking {
            val list = dao.getEvents()
            Assert.assertEquals(3, list.size)
            Assert.assertEquals(1660130185.toLong(), list.find { it.id == "0fba6fab-f6a8-439a-a92a-402a73a6448f" }?.timestamp)
            Assert.assertEquals(2, list.find { it.id == "0fba6fab-f6a8-439a-a92a-402a73a6448f" }?.version)
            Assert.assertEquals(
                "{\"_v\":2,\"_i\":\"e14276a2-74cd-42bd-a082-5b577a574c76\",\"_e\":12,\"_t\":1660130180,\"_av\":\"6.5.2\",\"c\":\"screen\",\"a\":\"opened\",\"os\":\"android\",\"osv\":\"12\",\"d\":\"Google Pixel 3 XL\",\"s\":\"homeScreen\",\"vt\":\"9JRmAmEumSg=\"}",
                list.find { it.id == "e14276a2-74cd-42bd-a082-5b577a574c76" }?.jsonEvent)
        }

    }
}