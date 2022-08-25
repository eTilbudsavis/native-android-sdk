package com.tjek.sdk.publicationviewer
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
import com.tjek.sdk.api.remote.ResponseType

// States for the publication load process
sealed class PublicationLoadingState {

    // The sdk is loading the publication and all the data needed to display it
    object Loading : PublicationLoadingState()

    // The publication has been fetched and can be shown
    object Successful : PublicationLoadingState()

    // Error in some of the steps
    data class Failed(val error: ResponseType.Error) : PublicationLoadingState()
}