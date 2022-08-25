package com.tjek.sdk.eventstracker

import android.util.Base64
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.TjekPreferences
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit

fun timestamp() = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

/**
 * Generate the view token for the content shown to the user
 * @param data byte array that represent the data
 * @param salt salt for the hash
 * @return the first 8 bytes of the md5, encoded in base64
 */
fun generateViewToken(data: ByteArray, salt: String = TjekPreferences.installationId): String {
    try {
        // get the bytes of the salt
        val id = salt.toByteArray(Charsets.UTF_8)

        // create the byte array with all of data -> salt + data
        val payload = ByteArray(data.size + id.size)
        System.arraycopy(id, 0, payload, 0, id.size)
        System.arraycopy(data, 0, payload, id.size, data.size)

        // Create MD5 Hash
        val digest = MessageDigest.getInstance("MD5")
        digest.update(payload)
        val digestResult = digest.digest()

        // take the first 8 bytes
        val md5 = Arrays.copyOfRange(digestResult, 0, 8)

        // encode to base 64
        return Base64.encodeToString(md5, Base64.NO_WRAP)

    } catch (e: Exception) {
        TjekLogCat.printStackTrace(e)
    }
    return ""
}