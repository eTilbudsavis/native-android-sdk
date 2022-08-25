package com.tjek.sdk.publicationviewer

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