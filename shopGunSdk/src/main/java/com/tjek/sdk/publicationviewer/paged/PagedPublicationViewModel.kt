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
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.publicationviewer.PublicationLoadingState
import com.tjek.sdk.publicationviewer.paged.libs.verso.VersoTapInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class PagedPublicationViewModel : ViewModel() {

    private val _publication = MutableLiveData<PublicationV2>()
    val publication: LiveData<PublicationV2>
        get() = _publication

    private val _pages = MutableLiveData<List<PublicationPageV2>>()
    val pages: LiveData<List<PublicationPageV2>>
        get() = _pages

    private val _hotspots = MutableLiveData<List<PublicationHotspotV2>>()
    val hotspots: LiveData<List<PublicationHotspotV2>>
        get() = _hotspots

    private val _loadingState = MutableLiveData<PublicationLoadingState>()
    val loadingState: LiveData<PublicationLoadingState>
        get() = _loadingState

    fun loadPublication(publication: PublicationV2) {
        _publication.postValue(publication)
        _loadingState.postValue(PublicationLoadingState.Loading)
        fetchPagesAndHotspots(publication)
    }

    fun loadPublication(publicationId: Id) {
        _loadingState.postValue(PublicationLoadingState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            when(val res = TjekAPI.getPublication(publicationId)) {
                is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(res))
                is ResponseType.Success -> loadPublication(res.data)
            }
        }
    }

    private fun fetchPagesAndHotspots(publication: PublicationV2) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                val pagesCall = async { TjekAPI.getPublicationPages(publication.id, publication.aspectRatio) }
                val hotspotsCall = async { TjekAPI.getPublicationHotspots(publication.id, publication.width, publication.height) }
                val pagesData = try {
                    pagesCall.await()
                } catch (e: Exception) {
                    ResponseType.Error(message = e.message)
                }
                val hotspotsData = try {
                    hotspotsCall.await()
                } catch (e: Exception) {
                    ResponseType.Error(message = e.message)
                }

                when (pagesData) {
                    is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(pagesData))
                    is ResponseType.Success -> {
                        _pages.postValue(pagesData.data)
                        if (hotspotsData is ResponseType.Success) {
                            _hotspots.postValue(hotspotsData.data)
                        }
                        _loadingState.postValue(PublicationLoadingState.Successful)
                    }
                }

            }
        }
    }

    fun findHotspot(tap: VersoTapInfo, hasIntro: Boolean): List<PublicationHotspotV2> {
        val hotspots = _hotspots.value
        if (hotspots?.isNotEmpty() == true && tap.isContentClicked()) {
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