package com.tjek.sdk.publicationviewer.paged

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.api.models.PublicationPageV2
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoTapInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.*

sealed class PublicationLoadingState {
    object Loading : PublicationLoadingState()
    object Successful : PublicationLoadingState()
    data class Failed(val error: ErrorType) : PublicationLoadingState()
}

class PagedPublicationViewModel : ViewModel() {

    private var publicationId: Id? = null
    var publication: PublicationV2?  = null
    var pages: List<PublicationPageV2> = emptyList()
    var hotspots: List<PublicationHotspotV2> = emptyList()

    private val _loadingState = MutableLiveData<PublicationLoadingState>(PublicationLoadingState.Loading)
    val loadingState: LiveData<PublicationLoadingState>
        get() = _loadingState

    fun loadPublication(publication: PublicationV2) {
        this.publication = publication
        publicationId = publication.id
        fetchPagesAndHotspots()
    }

    fun loadPublication(publicationId: Id) {
        this.publicationId = publicationId
        viewModelScope.launch(Dispatchers.IO) {
            when(val res = TjekAPI.getPublication(publicationId)) {
                is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(res.errorType))
                is ResponseType.Success -> {
                    publication = res.data
                    fetchPagesAndHotspots()
                }
            }
        }
    }

    private fun fetchPagesAndHotspots() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                val pagesCall = async { TjekAPI.getPublicationPages(publicationId!!, publication!!.aspectRatio) }
                val hotspotsCall = async { TjekAPI.getPublicationHotspots(publicationId!!, publication!!.width, publication!!.height) }
                val pagesData = try {
                    pagesCall.await()
                } catch (e: Exception) {
                    ResponseType.Error(ErrorType.Unknown(message = e.message))
                }
                val hotspotsData = try {
                    hotspotsCall.await()
                } catch (e: Exception) {
                    ResponseType.Error(ErrorType.Unknown(message = e.message))
                }

                when (pagesData) {
                    is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(pagesData.errorType))
                    is ResponseType.Success -> {
                        pages = pagesData.data ?: emptyList()
                        if (hotspotsData is ResponseType.Success) {
                            hotspots = hotspotsData.data ?: emptyList()
                        }
                        _loadingState.postValue(PublicationLoadingState.Successful)
                    }
                }

            }
        }
    }

    fun findHotspot(tap: VersoTapInfo, hasIntro: Boolean): List<PublicationHotspotV2> {
        if (hotspots.isNotEmpty() && tap.isContentClicked()) {
            val pages: IntArray = tap.pages.copyOf(tap.pages.size)
            val introOffset = if (hasIntro) -1 else 0
            for (i in pages.indices) {
                pages[i] = pages[i] + introOffset
            }
            val pageTapped: Int = tap.pageTapped + introOffset
            val list = mutableListOf<PublicationHotspotV2>()
            val length = pages.size.toFloat()
            val xOnClickedPage = (tap.getPercentX() % (1f / length)) * length
            for (h in hotspots) {
                if (h.hasLocationAt(pages, pageTapped, xOnClickedPage, tap.getPercentY())) {
                    list.add(h)
                }
            }
            return list
        }
        return emptyList()
    }
}