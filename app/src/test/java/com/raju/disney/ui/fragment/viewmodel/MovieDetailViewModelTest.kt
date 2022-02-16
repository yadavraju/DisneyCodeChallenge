package com.raju.disney.ui.fragment.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.raju.disney.TestCoroutineRule
import com.raju.disney.api.repository.BookRepository
import com.raju.disney.data.BookData
import com.raju.disney.data.ImageThumbUri
import com.raju.disney.ui.adapter.CommonAdapter
import com.raju.disney.ui.factory.AppFactory
import com.raju.disney.util.SOMETHING_WENT_WRONG
import kotlinx.coroutines.flow.Flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class MovieDetailViewModelTest {
  @get:Rule val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

  @get:Rule val testCoroutineRule = TestCoroutineRule()

  @Mock private lateinit var repository: BookRepository

  @Mock private lateinit var adapter: CommonAdapter

  @Mock private lateinit var appFactory: AppFactory

  @Mock private lateinit var bookDataFlow: Flow<BookData>

  private lateinit var testObject: MovieDetailViewModel

  @Before
  fun setUp() {
    testObject = MovieDetailViewModel(repository, adapter, appFactory)
  }

  @Test
  fun fetchBookData() {
    testCoroutineRule.runBlockingTest {
      Mockito.doReturn(bookDataFlow).`when`(repository).getBookData(1308)
      testObject.fetchBook(1308)
      assertEquals(true, testObject.isLoading().get())
    }
  }

  @Test
  fun setCharacterAdapterData() {
    val characterList = listOf(ImageThumbUri("jpg", "testUrl"), ImageThumbUri("jpg", "testUrl1"))
    //testObject.setCharacterAdapterData(characterList, null)
    verify(adapter).setDataBoundAdapter(any())
    assertNotNull(testObject.getCharacterAdapter())
  }

  @Test
  fun showExceptionMessage() {
    testObject.showExceptionMessage(SOMETHING_WENT_WRONG)
    assertNotNull(testObject.showErrorMessage)
  }
}
