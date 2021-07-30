package com.raju.disney.ui.fragment.viewmodel

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.raju.disney.api.repository.BookRepository
import com.raju.disney.base.BaseViewModel
import com.raju.disney.data.BookData
import com.raju.disney.data.ImageThumbUri
import com.raju.disney.ui.adapter.CommonAdapter
import com.raju.disney.ui.factory.AppFactory
import com.raju.disney.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val TAG: String = "MainViewModel"

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: BookRepository,
    private val adapter: CommonAdapter,
    private val appFactory: AppFactory
) : BaseViewModel() {

  private val characterAdapterObservableField: ObservableField<CommonAdapter> = ObservableField()
  private val loadingObservableField: ObservableField<Boolean> = ObservableField()

  val showErrorMessage: SingleLiveEvent<String?> by lazy { SingleLiveEvent() }
  val displayBookData: SingleLiveEvent<BookData> by lazy { SingleLiveEvent() }

  fun fetchBook(comicId: Int) {
    viewModelScope.launch {
      loadingObservableField.set(true)
      repository
          .getBookData(comicId)
          .catch { e -> handleException(TAG, e, loadingObservableField) }
          .collect {
            displayBookData.value = it
            setCharacterAdapterData(it.data.results[0].characterImages)
            loadingObservableField.set(false)
          }
    }
  }

  fun setCharacterAdapterData(characterImages: List<ImageThumbUri>) {
    val viewModels = characterImages.map { appFactory.createCharacterAdapter(it) }
    adapter.setDataBoundAdapter(viewModels)
    characterAdapterObservableField.set(adapter)
  }

  override fun showExceptionMessage(message: String?) {
    showErrorMessage.value = message
  }

  fun getCharacterAdapter(): ObservableField<CommonAdapter> = characterAdapterObservableField

  fun isLoading(): ObservableField<Boolean> = loadingObservableField
}
