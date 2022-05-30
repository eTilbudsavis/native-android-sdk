package com.shopgun.android.sdk

import com.tjek.sdk.api.TjekAPI
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NetworkTest {

    @Test
    fun test() {
        runBlocking {
            val list = TjekAPI.getCatalogs()
            Assert.assertEquals(list.isNotEmpty(), true)
        }
    }
}