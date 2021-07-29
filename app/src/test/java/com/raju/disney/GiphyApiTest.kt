package com.raju.disney

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.GsonBuilder
import com.raju.disney.api.GiphyApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(MockitoJUnitRunner::class)
class GiphyApiTest {
  @Rule
  @JvmField val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var service: GiphyApi

  private lateinit var mockWebServer: MockWebServer

  @Before
  fun createService() {
    mockWebServer = MockWebServer()
    service =
      Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()
        .create(GiphyApi::class.java)
  }

  @After
  fun stopService() {
    mockWebServer.shutdown()
  }

  @Test
  fun getPostsTest() = runBlocking {
    enqueueResponse("mock_giphy_search_response.json")
    val response = service.searchGiphy("Android", limit = 1)

    Assert.assertNotNull(response)
    Assert.assertEquals(response.giphyGDataList.size, 1)
  }

  private fun enqueueResponse(fileName: String, headers: Map<String, String> = emptyMap()) {
    val inputStream = javaClass.classLoader!!.getResourceAsStream("api-response/$fileName")
    val source = inputStream.source().buffer()
    val mockResponse = MockResponse()
    for ((key, value) in headers) {
      mockResponse.addHeader(key, value)
    }
    mockWebServer.enqueue(mockResponse.setBody(source.readString(Charsets.UTF_8)))
  }
}
