package com.shopgun.android.sdk

import com.tjek.sdk.eventstracker.generateViewToken
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ViewTokenGenerationTest {
    private val clientId = "myhash"
    private val regression_clientId = "0c0bba80-65cf-480b-9340-3add6725d5bf"

    @Test
    fun testViewTokenGenerator_1() {
        Assert.assertEquals("GKtJxfAxRZI=",
            generateViewToken(pagePublicationPageOpenViewToken("pub1", 1), clientId))
    }

    @Test
    fun testViewTokenGenerator_2() {
        val data = "😁"
        Assert.assertEquals("POcLWv7/N4Q=",
            generateViewToken(data.toByteArray(), clientId))
    }

    @Test
    fun testViewTokenGenerator_3() {
        val data = "my search string"
        Assert.assertEquals("bNOIlf+nAAU=",
            generateViewToken(data.toByteArray(), clientId))
    }

    @Test
    fun testViewTokenGenerator_4() {
        val data = "my search string 😁"
        Assert.assertEquals("+OJqwh68nIk=",
            generateViewToken(data.toByteArray(), clientId))
    }

    @Test
    fun testViewTokenGenerator_5() {
        val data = "øl og æg"
        Assert.assertEquals("NTgj68OWnbc=",
            generateViewToken(data.toByteArray(), clientId))
    }

    @Test
    fun testViewTokenGenerator_6() {
        Assert.assertEquals("VwMOrDD8zMk=",
            generateViewToken(pagePublicationPageOpenViewToken("pub1", 9999), clientId))
    }

    @Test
    fun regressionTest_1() {
        val pp_id = "920fujf"
        Assert.assertEquals("6vZz4FedqNQ=",
            generateViewToken(pp_id.toByteArray(), regression_clientId))
    }

    @Test
    fun regressionTest_2() {
        val of_id = "8818fZWd"
        Assert.assertEquals("mGzI8JcNv+Y=",
            generateViewToken(of_id.toByteArray(), regression_clientId))
    }

    @Test
    fun regressionTest_3() {
        Assert.assertEquals("Xgl5XTmr2Tw=",
            generateViewToken(pagePublicationPageOpenViewToken("3114tkf", 2),
                regression_clientId))
    }

    @Test
    fun regressionTest_4() {
        val query = "myDog&cat :)"
        Assert.assertEquals("PS00xHQ7Oxo=",
            generateViewToken(query.toByteArray(), regression_clientId))
    }

    @Test
    fun regressionTest_5() {
        val query = " <> %$#$6843135%%^%&"
        Assert.assertEquals("35nRscRCCwE=",
            generateViewToken(query.toByteArray(), regression_clientId))
    }

    @Test
    fun regressionTest_6() {
        Assert.assertEquals("dd9/Kp1699E=",
            generateViewToken(pagePublicationPageOpenViewToken("9b47F8f", 676),
                regression_clientId))
    }

    // For simplicity, this piece of code has been copied from the function
    private fun pagePublicationPageOpenViewToken(publicationId: String, pageNumber: Int): ByteArray {
        val b = ByteBuffer.allocate(4)
        b.order(ByteOrder.BIG_ENDIAN)
        b.putInt(pageNumber)
        val pageBytes = b.array()
        val ppIdBytes = publicationId.toByteArray(Charsets.UTF_8)
        val viewTokenContent = ByteArray(ppIdBytes.size + pageBytes.size)
        System.arraycopy(ppIdBytes, 0, viewTokenContent, 0, ppIdBytes.size)
        System.arraycopy(pageBytes, 0, viewTokenContent, ppIdBytes.size, pageBytes.size)
        return viewTokenContent
    }
}