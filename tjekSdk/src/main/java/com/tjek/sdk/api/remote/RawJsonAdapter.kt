package com.tjek.sdk.api.remote
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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