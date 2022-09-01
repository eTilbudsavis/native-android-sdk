package com.tjek.sdk.api.models
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

import android.net.Uri
import android.os.Parcelable
import com.tjek.sdk.api.Id
import kotlinx.parcelize.Parcelize

typealias IncitoViewId = String

@Parcelize
data class IncitoOffer(
        val viewId: IncitoViewId,
        val title: String,
        val description: String?,
        val link: Uri?,
        val featureLabels: List<String>?,     // tag related to the offer to customize user experience
        val publicationId: Id = ""
) : Parcelable