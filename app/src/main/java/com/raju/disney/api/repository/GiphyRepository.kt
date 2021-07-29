package com.raju.disney.api.repository

import co.betterup.betterfeed.api.repository.NetworkBoundRepository
import com.raju.disney.BuildConfig
import com.raju.disney.api.GiphyApi
import com.raju.disney.data.BookData
import com.raju.disney.data.GiphyData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Singleton
class GiphyRepository @Inject constructor(private val api: GiphyApi) {

  fun searchGiphy(query: String): Flow<GiphyData> {
    return object : NetworkBoundRepository<GiphyData>() {
          override suspend fun fetchFromRemote(): GiphyData = api.searchGiphy(query)
        }.asFlow()
  }

  fun getBookData(comicId: Int): Flow<BookData> {
    return object : NetworkBoundRepository<BookData>() {
      override suspend fun fetchFromRemote(): BookData = api.getBookData(comicId)
    }.asFlow()
  }
}
