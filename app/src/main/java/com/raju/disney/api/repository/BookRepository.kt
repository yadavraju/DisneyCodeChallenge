package com.raju.disney.api.repository

import com.raju.disney.api.BookApi
import com.raju.disney.data.BookData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

class BookRepository @Inject constructor(private val api: BookApi) {

  fun getBookData(comicId: Int): Flow<BookData> {
    return object : NetworkBoundRepository<BookData>() {
      override suspend fun fetchFromRemote(): BookData = api.getBookData(comicId)
    }.asFlow()
  }
}
