package com.tjek.sdk.api.remote.models.v2

import com.tjek.sdk.api.*
import com.tjek.sdk.api.models.*

fun PublicationV2.toPublication(): Publication {
    // sanity check on the dates
    val fromDate = runFromDateStr?.parse() ?: distantPast()
    val tillDate = runTillDateStr?.parse() ?: distantFuture()

    return Publication(
        id = id ?: "",
        width = dimensions?.width ?: 1f,
        height = dimensions?.height ?: 1f,
        offerCount = offerCount ?: 0,
        pageCount = pageCount ?: 0,
        label = label ?: "",
        isAvailableInAllStores = allStores ?: true,
        businessId = dealerId ?: "",
        storeId = storeId  ?: "",
        branding = branding?.toBranding() ?: Branding(),
        frontPageImageUrls = frontPageImageUrls?.toImagesUrl() ?: ImageUrls(),
        types = types?.toListOfPublicationTypes() ?: listOf(PublicationTypes.Paged),
        runDateRange = ValidityDateRange(fromDate, tillDate)
    )
}

fun BrandingV2.toBranding(): Branding {
    return Branding(
        name = name ?: "",
        websiteUrl = website ?: "",
        description = description ?: "",
        logoURL = logoURL ?: "",
        hexColor = color ?: 0
    )
}

fun ImageUrlsV2.toImagesUrl(): ImageUrls {
    return ImageUrls(
        view = view ?: "",
        zoom = zoom ?: "",
        thumb = thumb ?: ""
    )
}

fun List<PublicationTypesV2>.toListOfPublicationTypes(): List<PublicationTypes> {
    return when {
        size == 1 && contains(PublicationTypesV2.paged) -> listOf(PublicationTypes.Paged)
        size == 1 && contains(PublicationTypesV2.incito) -> listOf(PublicationTypes.Incito)
        containsAll(listOf(PublicationTypesV2.paged, PublicationTypesV2.incito)) -> listOf(PublicationTypes.Paged, PublicationTypes.Incito)
        else -> listOf(PublicationTypes.Paged)
    }
}

fun DealerV2.toBusiness(): Business {
    return Business(
        id = id ?: "",
        name = name ?: "",
        websiteUrl = website ?: "",
        description = description ?: "",
        descriptionMarkdown = descriptionMarkdown ?: "",
        logoOnWhiteUrl = logoOnWhiteUrl ?: "",
        logoOnBrandColorUrl = pageFlip?.logoURL ?: "",
        brandHexColor = color ?: 0,
        country = country?.id ?: ""
    )
}