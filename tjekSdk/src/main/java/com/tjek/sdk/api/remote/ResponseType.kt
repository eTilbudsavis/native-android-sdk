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
import com.tjek.sdk.api.remote.request.PaginatedRequestV2

sealed class ResponseType<out T : Any> {

    class Success<out T : Any>(val data: T) : ResponseType<T>()

    class Error(val code: Int? = null, val message: String?) : ResponseType<Nothing>() {
        override fun toString(): String {
            return "Error with code = $code. $message"
        }
    }
}

data class PaginatedResponse<T>(
    val results: T,
    val pageInfo: PageInfo
) {
    companion object {
        fun <T> v2PaginatedResponse(request: PaginatedRequestV2, response: List<T>): PaginatedResponse<List<T>> {
            return if (response.isEmpty()) {
                PaginatedResponse(response, PageInfo(request.startCursor.toString(), false))
            } else {
                PaginatedResponse(response, PageInfo(
                    lastCursor = (request.startCursor + response.size).toString(),
                    hasNextPage = response.size == request.itemCount))
            }
        }
    }
}

data class PageInfo(
    val lastCursor: String,
    val hasNextPage: Boolean
)