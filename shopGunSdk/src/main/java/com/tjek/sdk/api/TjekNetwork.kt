package com.tjek.sdk.api

import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.mappers.V2Mapper
import com.tjek.sdk.api.models.Publication
import com.tjek.sdk.api.remote.RetrofitClient
import com.tjek.sdk.api.remote.service.PublicationService
import java.lang.Exception

internal object TjekNetwork {

    private val publicationService = RetrofitClient.getClient().create(PublicationService::class.java)

    suspend fun getCatalogs(): List<Publication> {
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
}