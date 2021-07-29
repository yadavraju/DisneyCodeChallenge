package com.raju.disney.api

import com.raju.disney.api.repository.QueryParams.getQueryParams
import com.raju.disney.data.BookData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface BookApi {

  @GET("public/comics/{comicId}")
  suspend fun getBookData(
      @Path("comicId") comicId: Int,
      @QueryMap() params: Map<String, String> = getQueryParams()
  ): BookData
}
