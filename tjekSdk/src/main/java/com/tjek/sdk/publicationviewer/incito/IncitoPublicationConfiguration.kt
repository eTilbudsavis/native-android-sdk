package com.tjek.sdk.publicationviewer.incito
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
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Available customisable settings
@Parcelize
data class IncitoPublicationConfiguration(

    // Use the brand colors (e.g background). Set to false if you don't want to use them
    val useBrandColor: Boolean = true,

    // Initial vertical offset
    val initialVerticalOffset: Int = 0,

    // Set an id if you want to open the incito at a specific offer
    val openAtViewWithId: String? = null,

    // Feature label recorded by your app in previous incito sessions
    val recordedFeatureLabel: ArrayList<String>? = null

): Parcelable
