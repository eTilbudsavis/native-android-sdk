package com.tjek.sdk.publicationviewer.incito

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.IncitoOffer
import com.tjek.sdk.api.models.IncitoViewId
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.remote.ErrorType
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.request.FeatureLabel
import com.tjek.sdk.api.IncitoData
import com.tjek.sdk.api.remote.request.IncitoDeviceCategory
import com.tjek.sdk.api.remote.request.IncitoOrientation
import com.tjek.sdk.publicationviewer.PublicationLoadingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncitoPublicationViewModel : ViewModel() {

    private val _publication = MutableLiveData<PublicationV2>()
    val publication: LiveData<PublicationV2>
        get() = _publication

    private val _incitoData = MutableLiveData<IncitoData>()
    val incitoData: LiveData<IncitoData>
        get() = _incitoData

    private val _offers = MutableLiveData<Map<IncitoViewId, IncitoOffer>?>()
    val offers: LiveData<Map<IncitoViewId, IncitoOffer>?>
        get() = _offers

    private val _loadingState = MutableLiveData<PublicationLoadingState>()
    val loadingState: LiveData<PublicationLoadingState>
        get() = _loadingState

    fun loadPublication(
        publication: PublicationV2,
        deviceCategory: IncitoDeviceCategory,
        orientation: IncitoOrientation,
        pixelRatio: Float,
        maxWidth: Int,
        featureLabels: List<FeatureLabel>?,
        locale: String?
    ) {
        _publication.postValue(publication)
        if (publication.hasIncitoPublication) {
            _loadingState.postValue(PublicationLoadingState.Loading)
            getIncitoData(publication.id, deviceCategory, orientation, pixelRatio, maxWidth, featureLabels, locale)
        } else {
            _loadingState.postValue(PublicationLoadingState.Failed(ErrorType.NotAnIncitoPublication))
        }
    }

    fun loadPublication(
        id: Id,
        deviceCategory: IncitoDeviceCategory,
        orientation: IncitoOrientation,
        pixelRatio: Float,
        maxWidth: Int,
        featureLabels: List<FeatureLabel>?,
        locale: String?
    ) {
        _loadingState.postValue(PublicationLoadingState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            when(val res = TjekAPI.getPublication(id)) {
                is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(res.errorType))
                is ResponseType.Success -> loadPublication(res.data!!, deviceCategory, orientation, pixelRatio, maxWidth, featureLabels, locale)
            }
        }
    }

    fun getOfferFromMap(viewId: IncitoViewId): IncitoOffer? = _offers.value?.get(viewId)?.copy(publicationId = publication.value?.id ?: "")

    private fun getIncitoData(
        id: Id,
        deviceCategory: IncitoDeviceCategory,
        orientation: IncitoOrientation,
        pixelRatio: Float,
        maxWidth: Int,
        featureLabels: List<FeatureLabel>?,
        locale: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val res = TjekAPI.getIncito(id, deviceCategory, orientation, pixelRatio, maxWidth, featureLabels, locale)) {
                is ResponseType.Error -> _loadingState.postValue(PublicationLoadingState.Failed(res.errorType))
                is ResponseType.Success -> {
                    _loadingState.postValue(PublicationLoadingState.Successful)
                    _incitoData.postValue(res.data!!)
                    createOfferMap(res.data)
                }
            }
        }
    }

    // Go through the json data looking for offers
    private suspend fun createOfferMap(incitoJson: String) {
        _offers.postValue(parseIncitoJson(incitoJson))
    }
}