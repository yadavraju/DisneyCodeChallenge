package com.raju.disney.ui.factory

import com.raju.disney.data.GData
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppFactoryTest {

  lateinit var testObject: AppFactory

  @Before
  fun setUp() {
    testObject = AppFactory()
  }

  @Test
  fun createGiphyAdapter() {
    assertNotNull(testObject.createGiphyAdapter(GData(), ::onItemClicked))
  }

  private fun onItemClicked(uri: String?) {}
}