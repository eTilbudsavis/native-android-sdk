package com.tjek.sdk.api

import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.mappers.V2Mapper
import com.tjek.sdk.api.models.Publication
import com.tjek.sdk.api.remote.APIClient
import com.tjek.sdk.api.remote.services.PublicationService
import java.lang.Exception

internal object NetworkRequest {

    private val publicationService: PublicationService by lazy { APIClient.getV2Client().create(PublicationService::class.java) }

    suspend fun getPublications(): List<Publication> {
        try {
            val response = publicationService.getCatalogs()
            if (response.isSuccessful) {
                response.body()?.let { list ->
                    return list.map {
                        V2Mapper.map(it)
                    }
                }
            }
            return emptyList()
        } catch (e: Exception) {
            TjekLogCat.e(e.printStackTrace().toString())
            return emptyList()
        }
    }

    suspend fun getPublication(publicationId: Id): Publication {
        try {
            val response = publicationService.getCatalog(publicationId)
            if (response.isSuccessful) {
                response.body()?.let { p ->
                    return V2Mapper.map(p)
                }
            }
            return Publication()
        } catch (e: Exception) {
            TjekLogCat.e(e.printStackTrace().toString())
            return Publication()
        }
    }
}