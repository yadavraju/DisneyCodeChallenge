package com.raju.disney.ui.fragment.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.raju.disney.TestCoroutineRule
import com.raju.disney.api.repository.BookRepository
import com.raju.disney.data.GiphyData
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

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
  @get:Rule val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

  @get:Rule val testCoroutineRule = TestCoroutineRule()

  @Mock private lateinit var repository: BookRepository

  @Mock private lateinit var adapter: CommonAdapter

  @Mock private lateinit var appFactory: AppFactory

  @Mock private lateinit var giphyDataFlow: Flow<GiphyData>

  private lateinit var testObject: MainViewModel

  @Before
  fun setUp() {
    testObject = MainViewModel(repository, adapter, appFactory)
  }

  @Test
  fun searchGiphy() {
    testCoroutineRule.runBlockingTest {
      Mockito.doReturn(giphyDataFlow).`when`(repository).getBookData(1308)
      testObject.fetchBook(1308)
      assertEquals(true, testObject.isLoading().get())
    }
  }

  @Test
  fun showExceptionMessage() {
    testObject.showExceptionMessage(SOMETHING_WENT_WRONG)
    assertNotNull(testObject.showErrorMessage)
  }
}
