package com.tjek.sdk.api.remote

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import okio.ByteString
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class RawJson

class RawJsonAdapter: JsonAdapter<ByteString>() {

    override fun toJson(writer: JsonWriter, value: ByteString?) {
        if (value == null) {
            writer.jsonValue(null)
            return
        }
        writer.value(ByteArrayInputStream(value.toByteArray()).source().buffer())
    }

    override fun fromJson(reader: JsonReader): ByteString? {
        return try {
            reader.nextSource().readByteString()
        } catch (e: Exception) {
            null
        }
    }
}