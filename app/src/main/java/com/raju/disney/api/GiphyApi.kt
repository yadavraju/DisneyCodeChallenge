package com.raju.disney.api

import com.raju.disney.BuildConfig
import com.raju.disney.api.repository.QueryParams.getQueryParams
import com.raju.disney.data.BookData
import com.raju.disney.data.GiphyData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface GiphyApi {

  // https://api.giphy.com/v1/gifs/search?api_key=htPBKkyWRy9T8xIw9pNNVkuHmPC2e66m&q=Android&limit=50&offset=0&rating=g&lang=en
  // Passing only query as dynamic for now all other parameter are default
  @GET("gifs/search")
  suspend fun searchGiphy(
      @Query("q") query: String,
      @Query("api_key") apiKey: String = BuildConfig.API_KEY,
      @Query("limit") limit: Int = 50,
      @Query("offset") offset: Int = 0,
      @Query("rating") rating: String = "g",
      @Query("lang") lang: String = "en"
  ): GiphyData

  // https://gateway.marvel.com/v1/public/comics/1308?ts=1627355040&apikey=10e88d985270066d379d5d616b0d9279&hash=46d81dc284d331bd846aa7df6d92ded0

  @GET("public/comics/{comicId}")
  suspend fun getBookData(
      @Path("comicId") comicId: Int,
      @QueryMap() params: Map<String, String> = getQueryParams()
  ): BookData

  //  @GET("public/comics/{comicId}")
  //  suspend fun getBookData(
  //    @Path("comicId") comicId: Int,
  //    @QueryMap() params: Map<String, String>,
  //    @Query("hash") hash: String = Md5HashGenerator.getMd5(BuildConfig.API_KEY),
  //    @Query("apikey") apiKey: String = BuildConfig.API_KEY,
  //    @Query("ts") timeStamp: Long = 1627355040L, // Currently I am passing these two as default
  // for now, but we need to pass current time stamp
  //  ): BookData
}
